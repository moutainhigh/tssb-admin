
package com.tssb.common.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DuiaRedisCacheManager extends AbstractTransactionSupportingCacheManager {

    private final Log logger = LogFactory.getLog(DuiaRedisCacheManager.class);

    @SuppressWarnings("rawtypes")//
    private final RedisTemplate template;
    private boolean usePrefix = false;
    private RedisCachePrefix cachePrefix = new DefaultRedisCachePrefix();
    private boolean loadRemoteCachesOnStartup = false;
    private boolean dynamic = true;

    // 0 - never expire
    private long defaultExpiration = 0;
    private Map<String, Long> expires = null;

    /**
     * Construct a {@link DuiaRedisCacheManager}.
     *
     * @param template
     */
    @SuppressWarnings("rawtypes")
    public DuiaRedisCacheManager(RedisTemplate template) {
        this(template, Collections.<String>emptyList());
    }

    /**
     * Construct a static {@link DuiaRedisCacheManager}, managing caches for the specified cache names only.
     *
     * @param template
     * @param cacheNames
     * @since 1.2
     */
    @SuppressWarnings("rawtypes")
    public DuiaRedisCacheManager(RedisTemplate template, Collection<String> cacheNames) {
        this.template = template;
        setCacheNames(cacheNames);
    }

    @SuppressWarnings("rawtypes")
    public DuiaRedisCacheManager(RedisTemplate template, boolean usePrefix, Collection<String> cacheNames, Map<String, Long> expires) {
        this.template = template;
        this.usePrefix = usePrefix;
        this.expires = expires;
        setCacheNames(cacheNames);
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = super.getCache(name);
        if (cache == null && this.dynamic) {
            return createAndAddCache(name);
        }

        return cache;
    }

    /**
     * Specify the set of cache names for this CacheManager's 'static' mode. <br/>
     * The number of caches and their names will be fixed after a call to this method, with no creation of further cache
     * regions at runtime.
     */
    public void setCacheNames(Collection<String> cacheNames) {
        if (!CollectionUtils.isEmpty(cacheNames)) {
            for (String cacheName : cacheNames) {
                createAndAddCache(cacheName);
            }
            this.dynamic = false;
        }
    }

    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
    }

    /**
     * Sets the cachePrefix. Defaults to 'DefaultRedisCachePrefix').
     *
     * @param cachePrefix the cachePrefix to set
     */
    public void setCachePrefix(RedisCachePrefix cachePrefix) {
        this.cachePrefix = cachePrefix;
    }

    /**
     * Sets the default expire time (in seconds).
     *
     * @param defaultExpireTime time in seconds.
     */
    public void setDefaultExpiration(long defaultExpireTime) {
        this.defaultExpiration = defaultExpireTime;
    }

    /**
     * Sets the expire time (in seconds) for cache regions (by key).
     *
     * @param expires time in seconds
     */
    public void setExpires(Map<String, Long> expires) {
        this.expires = (expires != null ? new ConcurrentHashMap<String, Long>(expires) : null);
    }

    /**
     * If set to {@code true} {@link DuiaRedisCacheManager} will try to retrieve cache names from redis server using
     * {@literal KEYS} command and initialize {@link RedisCache} for each of them.
     *
     * @param loadRemoteCachesOnStartup
     * @since 1.2
     */
    public void setLoadRemoteCachesOnStartup(boolean loadRemoteCachesOnStartup) {
        this.loadRemoteCachesOnStartup = loadRemoteCachesOnStartup;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.cache.support.AbstractCacheManager#loadCaches()
     */
    @Override
    protected Collection<? extends Cache> loadCaches() {

        Assert.notNull(this.template, "A redis template is required in order to interact with data store");
        return addConfiguredCachesIfNecessary(loadRemoteCachesOnStartup ? loadAndInitRemoteCaches() : Collections
                .<Cache>emptyList());
    }

    /**
     * Returns a new {@link Collection} of {@link Cache} from the given caches collection and adds the configured
     * {@link Cache}s of they are not already present.
     *
     * @param caches must not be {@literal null}
     * @return
     */
    protected Collection<? extends Cache> addConfiguredCachesIfNecessary(Collection<? extends Cache> caches) {

        Assert.notNull(caches, "Caches must not be null!");

        Collection<Cache> result = new ArrayList<Cache>(caches);

        for (String cacheName : getCacheNames()) {

            boolean configuredCacheAlreadyPresent = false;

            for (Cache cache : caches) {

                if (cache.getName().equals(cacheName)) {
                    configuredCacheAlreadyPresent = true;
                    break;
                }
            }

            if (!configuredCacheAlreadyPresent) {
                result.add(getCache(cacheName));
            }
        }

        return result;
    }

    protected Cache createAndAddCache(String cacheName) {
        addCache(createCache(cacheName));
        return super.getCache(cacheName);
    }

    @SuppressWarnings("unchecked")
    protected RedisCache createCache(String cacheName) {
        long expiration = computeExpiration(cacheName);
        return new RedisCache(cacheName, (usePrefix ? cachePrefix.prefix(cacheName) : null), template, expiration);
    }

    protected long computeExpiration(String name) {
        Long expiration = null;
        if (expires != null) {
            expiration = expires.get(name);
        }
        return (expiration != null ? expiration.longValue() : defaultExpiration);
    }

    protected List<Cache> loadAndInitRemoteCaches() {

        List<Cache> caches = new ArrayList<Cache>();

        try {
            Set<String> cacheNames = loadRemoteCacheKeys();
            if (!CollectionUtils.isEmpty(cacheNames)) {
                for (String cacheName : cacheNames) {
                    if (null == super.getCache(cacheName)) {
                        caches.add(createCache(cacheName));
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to initialize cache with remote cache keys.", e);
            }
        }

        return caches;
    }

    @SuppressWarnings("unchecked")
    protected Set<String> loadRemoteCacheKeys() {
        return (Set<String>) template.execute(new RedisCallback<Set<String>>() {

            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {

                // we are using the ~keys postfix as defined in RedisCache#setName
                Set<byte[]> keys = connection.keys(template.getKeySerializer().serialize("*~keys"));
                Set<String> cacheKeys = new LinkedHashSet<String>();

                if (!CollectionUtils.isEmpty(keys)) {
                    for (byte[] key : keys) {
                        cacheKeys.add(template.getKeySerializer().deserialize(key).toString().replace("~keys", ""));
                    }
                }

                return cacheKeys;
            }
        });
    }

    @SuppressWarnings("rawtypes")
    protected RedisTemplate getTemplate() {
        return template;
    }

    protected RedisCachePrefix getCachePrefix() {
        return cachePrefix;
    }

    protected boolean isUsePrefix() {
        return usePrefix;
    }
}

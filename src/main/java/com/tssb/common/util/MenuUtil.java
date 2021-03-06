package com.tssb.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单对象DTO,首页显示当前用户所属菜单使用.
 * Created by wangsongpeng on 2016/2/16.
 */
public class MenuUtil {
    private Long id;
    private String	menuName;		 /* 菜单名称 */
    private String	menuUrl;		 /* 菜单URL */
    private Boolean	menuEnable;    /* 菜单是否启用(1启用0不启用) */
    private String	menuIcon;		 /* 菜单的ICON图标 */
    private Boolean subIsExist;     /*是否有子菜单,true有就显示,false无不显示*/
    private String  menuCode; /*菜单代码*/
    private List<MenuUtil> subMenu = new ArrayList<MenuUtil>(); /*子菜单列表*/

    public MenuUtil() {

    }

    public MenuUtil(Long id, String menuName, String menuUrl, String menuIcon, String menuCode) {
        this.id = id;
        this.menuName = menuName;
        this.menuUrl = menuUrl;
        this.menuIcon = menuIcon;
        this.menuCode = menuCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public Boolean getMenuEnable() {
        return menuEnable;
    }

    public void setMenuEnable(Boolean menuEnable) {
        this.menuEnable = menuEnable;
    }

    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }

    public Boolean getSubIsExist() {
        return subIsExist;
    }

    public void setSubIsExist(Boolean subIsExist) {
        this.subIsExist = subIsExist;
    }

    public List<MenuUtil> getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(List<MenuUtil> subMenu) {
        this.subMenu = subMenu;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }
}

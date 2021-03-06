<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" contentType="text/html; UTF-8" %>
<%@include file="/WEB-INF/view/common/include/taglib.jsp" %>
<%-- 头部 --%>
<%-- begin container-fluid --%>
<div class="container-fluid">
    <%-- begin mobile sidebar expand / collapse button --%>
    <div class="navbar-header">
        <a href="javascript:;" class="navbar-brand"><span class="navbar-logo"></span>天山时报+</a>
        <button type="button" class="navbar-toggle" data-click="sidebar-toggled">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
    </div>
    <%-- end mobile sidebar expand / collapse button --%>

    <%-- begin header navigation right --%>
    <ul class="nav navbar-nav navbar-right">
        <li class="dropdown navbar-user">
            <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown">
                <img src="${pageContext.request.contextPath}/resources/image/tssb/common/user_head.jpg"  />
                <%-- 员工昵称 --%>
                <span class="hidden-xs">${sessionScope.login_user.username}</span><b class="caret"></b>
            </a>
            <ul class="dropdown-menu animated fadeInLeft">
                <li class="arrow"></li>
                <li><a href="${pageContext.request.contextPath}/user/passWordAndInfo">个人</a></li>
                <li class="divider"></li>
                <li>
                    <a href="${pageContext.request.contextPath}/authorityAdvices/index"><span class="badge badge-danger pull-right" id="head_msgCount"></span>消息</a>
                </li>
                <li class="divider"></li>
                <li><a href="javascript:void(0)" onclick="loginOut();">退出</a></li>
            </ul>
        </li>
    </ul>
    <%-- end header navigation right --%>
</div>
<script>
    function loginOut(){
        $.cookie('user_id','');
        window.location.href="/";
    }
</script>
<%-- end container-fluid --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Державний повірник</title>
    <link href="resources/assets/bower_components/ng-table/ng-table.css" rel="stylesheet">
    <link href="resources/assets/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/assets/bower_components/font-awesome/css/font-awesome.min.css" rel="stylesheet">
    <link href="resources/assets/css/calibrator.css" rel="stylesheet">
</head>

<body>

<div id="verificatorModule" class="wrapper">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0" ng-controller="TopNavBarController">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand">Централізована система повірки лічильників</a>
        </div>

        <!-- Nav bar top right links -->
        <ul class="nav navbar-top-links navbar-right">
            <li class="dropdown" dropdown>
                <a class="dropdown-toggle" dropdown-toggle>
                    <i class="fa fa-user fa-fw"></i><i class="fa fa-caret-down"></i>
                </a>
                <ul class="dropdown-menu dropdown-user">
                    <li><a><i class="fa fa-user fa-fw"></i> Профіль</a>
                    </li>
                    <li><a><i class="fa fa-gear fa-fw"></i> Налаштування</a>
                    </li>
                    <li class="divider"></li>
                    <li><a ng-click="logout()"><i class="fa fa-sign-out fa-fw"></i> Вийти</a>
                    </li>
                </ul>
            </li>
        </ul>

        <!-- Sidebar -->
        <div class="navbar-default sidebar" role="navigation">
            <div class="sidebar-nav navbar-collapse">
                <ul class="nav" id="side-menu">
                    <li ui-sref-active="active">
                        <a ui-sref="main-panel"><i class="fa fa-home fa-fw"></i> Головна панель</a>
                    </li>

                    <li ui-sref-active="active" ng-controller ="NotificationsController">
                        <a ui-sref="new-verifications" ng-click="reloadVerifications()" ><i class="fa fa-list-alt fa-fw"></i>Нові заявки   
        			 		<button  class="pull-right myCircleButton " ng-if="countOfUnreadVerifications > 0"
        			 			ng-bind="countOfUnreadVerifications" ng-cloak>
        			 		</button>
                      	</a>
                    </li>
					<sec:authorize url="/verificator/admin/">
                    <li ui-sref-active="active">
                        <a ui-sref="employees"><i class="fa fa-user-plus"></i>Додати працівника</a>
                    </li>
                      </sec:authorize>
                </ul>
            </div>
        </div>
    </nav>

    <div ui-view></div>

</div>

<script type="text/javascript" data-main="resources/app/verificator/runApp"
        src="resources/assets/bower_components/requirejs/require.js"></script>

</body>

</html>

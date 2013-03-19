<%@page contentType="text/html; charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Renew Page</title>
</head>
<body>
  <h1>Renew Page</h1>
  <div>This is an page which can only be viewed after relogon (not remeber me logon), this is usually used for important operations (such as payment.)</div>
  <div>---------------------------------------------------------</div>
  <div>To <a href="../../">public</a> page.</div>
  <div>To <a href="..">protected</a> page.</div>
  <div>To <a href="../admin">admin</a> page.</div>
  <div>---------------------------------------------------------</div>
  <div>Wecome ${empty pageContext.request.remoteUser ? "Guest" : pageContext.request.remoteUser}.</div>
  <div>session["_const_cas_assertion_"] = ${sessionScope["_const_cas_assertion_"]}</div>
  <div>request.userPricipal.name = ${pageContext.request.userPrincipal.name}</div>
  <div>request.userPricipal.class = ${pageContext.request.userPrincipal.class}</div>
  <div>request.userPricipal.attributes = ${pageContext.request.userPrincipal.attributes}</div>
</body>
</html>

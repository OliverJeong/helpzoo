<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입 - 회원 정보 입력</title>
</head>
<style>
#joinTxt{
font-family: "Recipekorea";
text-align : center;
}
</style>
<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<div class="row justify-content-md-center">
	<div id="joinTxt"><h1>회원가입</h1></div>
	</div>
	이메일 : <input type="email" name="memberEmail">
	<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
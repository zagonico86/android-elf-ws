<!DOCTYPE>
<html>
<head>
	<meta charset="UTF-8">
	<title>Test server</title>
</head>
<body>
	Html page to test the ws.

	<h3>Test get-only-json</h3>
	<form action="ws.php" method="get">
		action: <input type="text" name="action" value="get-only-json" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		<input type="submit" value="Send" />
	</form>
	
	<h3>Test post-only-json</h3>
	<form action="ws.php" method="post">
		action: <input type="text" name="action" value="post-only-json" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		<input type="submit" value="Send" />
	</form>
	
	<h3>Test post-file-json</h3>
	<form action="ws.php?getparam=1" method="post" enctype="multipart/form-data">
		action: <input type="text" name="action" value="post-file-json" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		file1: <input type="file" name="file1" /><br/>
		file2: <input type="file" name="file2" /><br/>
		<input type="submit" value="Send" />
	</form>
	
	<h3>Test get-only-file</h3>
	<form action="ws.php" method="get">
		action: <input type="text" name="action" value="get-only-file" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		<input type="submit" value="Send" />
	</form>
	
	<h3>Test post-only-file</h3>
	<form action="ws.php" method="post">
		action: <input type="text" name="action" value="post-only-file" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		<input type="submit" value="Send" />
	</form>
	
	<h3>Test post-file-file</h3>
	<form action="ws.php?getparam=1" method="post" enctype="multipart/form-data">
		action: <input type="text" name="action" value="post-file-file" readonly /><br/>
		param1: <input type="text" name="param1" value="test" /><br/>
		file1: <input type="file" name="file1" /><br/>
		file2: <input type="file" name="file2" /><br/>
		<input type="submit" value="Send" />
	</form>
</body>
</html>
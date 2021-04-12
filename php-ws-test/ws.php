<?php
/**
 * Simple web service to test the ElfWs library.
 */
function saveInFile($action) {
	ob_start();
	echo "GET:\n";
	var_dump($_GET);
	echo "\nPOST:\n";
	var_dump($_POST);
	echo "\nFILE:\n";
	var_dump($_FILES);
	
	$content = ob_get_clean();

	if (!file_exists('outputs')) mkdir('outputs');
	
	file_put_contents('outputs/'.date('YmdHis').'_'.$action.'.txt', $content);
	
	if (count($_FILES)) {
		foreach ($_FILES as $k => $v) {
			move_uploaded_file($v['tmp_name'], 'outputs/'.date('YmdHis').'_'.$v['name']);
		}
	}
}

function returnJSON($ris) {
	header('Content-Type: application/json');
	
	die(json_encode($ris));
}

function download($filepath, $filename, $mime='') {
	if (!$mime) {
		$finfo = finfo_open(FILEINFO_MIME_TYPE); 
		header('Content-Type: '.finfo_file($finfo, $filepath));
	}
	else {
		header('Content-Type: '.$mime);
	}
	header('Content-Disposition: attachment; filename="'.$filename.'"');

	readfile($filepath);
	die();
}

$ret = ['status' =>  'error', 'message' => 'action not found'];

$action = array_key_exists('action', $_POST) ? $_POST['action'] : (array_key_exists('action', $_GET) ? $_GET['action'] : false);

switch ($action) {
	case 'get-only-json':
		saveInFile($action);
		$ret = [ 'status' => 'success', 'message' => $action.' ok' ];
		break;
	case 'post-only-json':
		saveInFile($action);
		$ret = [ 'status' => 'success', 'message' => $action.' ok' ];
		break;
	case 'post-file-json':
		saveInFile($action);
		$ret = [ 'status' => 'success', 'message' => $action.' ok' ];
		break;
	case 'get-only-file':
		saveInFile($action);
		
		header('Content-Type: text/html');
		header('Content-Disposition: attachment; filename="example.html"');
		echo "<!DOCTYPE><html><body><pre>";
		echo "GET:\n";
		var_dump($_GET);
		echo "\nPOST:\n";
		var_dump($_POST);
		echo "\nFILE:\n";
		var_dump($_FILES);
		echo "</pre></body></html>";
		die();
		
		break;
	case 'post-only-file':
		saveInFile($action);
		
		header('Content-Type: text/plain');
		header('Content-Disposition: attachment; filename="example.txt"');
		echo "GET:\n";
		var_dump($_GET);
		echo "\nPOST:\n";
		var_dump($_POST);
		echo "\nFILE:\n";
		var_dump($_FILES);
		die();
		
		break;
	case 'post-file-file':
		saveInFile($action);
		
		download('inputs/test.png', 'image.png', 'image/png');
		break;
}

returnJSON($ret);
<?php

define('__PROJECT_ROOT__', dirname(__FILE__));
//require_once(__PROJECT_ROOT__.'/connector.php');
require_once('connector.php');
require_once('utils.php');


function success($message){
  $json = array(
    'success' => $message,
  );
  echo(json_encode($json));
}


  $action = $_POST['action'];

  $con = '';

  try {
     $con = connect();

     if ($action == "sendphoto"){
         // subir foto

         $base = $_POST['image'];
         $username = $_POST['username'];

         $filename = time();

         $binary = base64_decode($base);
         file_put_contents($filename.".jpg", $binary);

         $actual_link = "https://134.209.235.115/ebracamonte001/WEB/pomodoro/".$filename.".jpg";

         execute($con, "UPDATE users SET imagepath='".$actual_link."' WHERE username='".$username."'");

         success("ok");
      }

   } catch (Exception $e) {
     // error
     $json = array(
       'error' => $e->getMessage()
     );
     echo(json_encode($json));
   }

  close($con);
?>

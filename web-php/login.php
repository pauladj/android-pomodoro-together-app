<?php

define('__PROJECT_ROOT__', dirname(__FILE__));
//require_once(__PROJECT_ROOT__.'/connector.php');
require_once('connector.php');


function success($message){
  $json = array(
    'success' => $message,
  );
  echo(json_encode($json));
}



  $parametros=json_decode(file_get_contents('php://input'),true);


  $username=$parametros["username"];
  $password=$parametros["password"];

  $con = '';

  try {
     $con = connect();

     $resultado = execute($con, "SELECT username FROM users WHERE username='".$username."' AND password='".$password."'");

     if (!select_is_empty($resultado)) {
         // the user exists
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

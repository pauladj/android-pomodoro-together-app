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

  $email=$parametros["email"];
  $username=$parametros["username"];
  $password=$parametros["password"];

  $con = '';

  try {
     $con = connect();

     // check if username exists
     $resultado = execute($con, "SELECT username FROM users WHERE username='".$username."' AND password='".$password."'");

     if (select_is_empty($resultado)) {
        // save the new user
        execute($con, "INSERT INTO users(username, password, email)
                 VALUES ('".$username."', '".$password."', '".$email."')");
        success("ok");
     }else{
       throw new Exception('username_exists');
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

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



  $parametros=json_decode(file_get_contents('php://input'),true);


  $username=$parametros["username"];
  $password=$parametros["password"];
  $token=$parametros["token"];

  $con = '';

  try {
     $con = connect();

     $resultado = execute($con, "SELECT username, token FROM users WHERE username='".$username."' AND password='".$password."'");

     if (!select_is_empty($resultado)) {
         // the user exists
         $row = mysqli_fetch_assoc($resultado);

         $lastToken = $row["token"];
         if ($lastToken == NULL){
           // primero en logearse con este username
           $resultado = execute($con, "UPDATE users SET token='".$token."' WHERE username='".$username."'");
         }else{
           // enviar mensaje fcm al último logeado con este user
           send_message_to_user($username, $lastToken);
           $resultado = execute($con, "UPDATE users SET token='".$token."' WHERE username='".$username."'");
         }

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

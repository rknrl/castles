<html>

<head>
    <meta charset="UTF-8">

    <title>Game</title>

    <script type="text/javascript" src="swfobject.js"></script>

    <script type="text/javascript" src="getflashvars.js"></script>

    <style>
        body {
            padding: 0;
            margin: 0;
        }
    </style>
    <script type="text/javascript">
        function flash() {
            return document.getElementById("flashContent");
        }

        window.fbAsyncInit = function () {
            FB.init({
                appId: '370173203168786',
                xfbml: true,
                version: 'v2.2'
            });

            function onLogin(response) {
                if (response.status == 'connected') {
                    flashVars.rknrlAccountType = "FACEBOOK";
                    flashVars.rknrlHost = "127.0.0.1";
                    flashVars.rknrlHttpPort = "8080";
                    flashVars.rknrlGamePort = "2335";
                    flashVars.rknrlPolicyPort = "2336";
                    flashVars.accessToken = response.authResponse.accessToken;
                    flashVars.expiresIn = response.authResponse.expiresIn;
                    flashVars.signedRequest = response.authResponse.signedRequest;
                    flashVars.userID = response.authResponse.userID;

                    swfobject.embedSWF("game.swf", "flashContent", "100%", "100%", "10.1.0", "expressInstall.swf", flashVars,
                        {
                            quality: "high",
                            allowFullScreenInteractive: 'true',
                            allowfullscreen: true
                        });
                }
            }

            // https://developers.facebook.com/docs/reference/javascript/FB.getLoginStatus
            FB.getLoginStatus(function (response) {
                if (response.status == 'connected') {
                    onLogin(response);
                } else {
                    FB.login(function (response) {
                        onLogin(response);
                    }, {scope: 'user_friends'});
                }
            });
        };

        (function (d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {
                return;
            }
            js = d.createElement(s);
            js.id = id;
            js.src = "//connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));

        function reloadPage() {
            document.location.reload(true)
        }
    </script>
</head>

<body>

<?php

list($encoded_sig, $payload) = explode('.', $_POST["signed_request"], 2);

$sig = base64_url_decode($encoded_sig);
$data = base64_url_decode($payload);

echo("sig:" . $sig);
echo("data:" . $data);

function base64_url_decode($input)
{
    return base64_decode(strtr($input, '-_', '+/'));
}

?>

<div id="flashContent">
    <p><a href="https://www.adobe.com/go/getflashplayer"><img
                src="https://www.adobe.com/images/shared/download_buttons/get_flash_player.gif"
                alt="Get Adobe Flash player"/></a></p>
</div>
</body>

</html>

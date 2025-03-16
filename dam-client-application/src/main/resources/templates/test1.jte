<html>
    <head>
        <title>Whatever</title>
        <style>
            body {
                margin: 0;
                padding: 0;
                display: flex;
            }

            #holder {
                display: flex;
                flex: 1;
                background-color: darkblue;
                flex-direction: column;
            }

            #nav {
                height: 100px;
                background-color: yellow;
                display: flex;
            }

            #nav > #logo {
                height: 100%;
                mix-blend-mode: darken;
            }

            #content {
                flex: 1;
                background-color: green;
            }

        </style>
    </head>

    <body>

        <div id="holder">
            <div id="nav">
                <img id="logo" src="/mivirim-logo.png">
            </div>
            <div id="content">
                <a href="test2">Go to Test 2</a>
            </div>
        </div>

    <!--

    <div class="header">Test DAM client. I am not in German. Please stop asking me that.</div>
    -->

    </body>

</html>
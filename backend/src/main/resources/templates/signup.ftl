<!DOCTYPE html>
<html data-theme="synthwave">
    <head>
        <link href="https://cdn.jsdelivr.net/npm/daisyui@2.51.6/dist/full.css" rel="stylesheet" type="text/css" />
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <title>Home</title>
    </head>
    <header>
    </header>
    <body>
        <!-- make box in the middle of the screen -->
        <div class="flex flex-col items-center justify-center h-screen">
            <#if message??>
            <div class="alert shadow-lg alert-error" style="width:500px; margin-bottom:100px;">
                <div>
                    <span>
                            ${message}
                    </span>
                </div>
            </div>
            </#if>
            <!-- make box with rounded corners -->
            <div class="w-96 p-6 space-y-6 bg-violet-950 rounded-xl shadow-md">
                <!-- make box with rounded corners -->
                <div class="flex flex-col items-center justify-center space-y-6">
                    <h1 class="text-3xl">
                        Sign Up 
                    </h1>
                    <form action="/signup" method="POST" >
                        <input type="username" placeholder="Username" name="username" class="input input-bordered input-secondary w-full max-w-xs" style="" id="usernameinput" />
                        <label class="label">
                            <span class="label-text-alt text-error hidden" id="usernamemessage">Username must be under 16 characters</span>
                            <span class="label-text-alt text-error hidden" id="usernamemessage2">Username is required</span>
                        </label>
                        <input type="password" placeholder="Password" name="password" class="input input-bordered input-secondary w-full max-w-xs" style="" id="passwordinput" />
                        <label class="label">
                        </label>
                        <input type="password" placeholder="Repeat Password" name="rpassword" class="input input-bordered input-secondary w-full max-w-xs" style="margin-bottom: 10px" />
                        <br>
                        <button type="submit" class="btn btn-primary btn-active w-full max-w-xs">Sign up</button>
                    </form>
                    <p class="text-xl">Already signed up? <a class="link-info" href="/login">Log in</a>!</p>
                </div>
            </div>
        </div>
    </body>
    <script>
        $("#usernameinput").change(function(){
            if ($("#usernameinput").val().length > 16) {
                $("#usernamemessage").removeClass("hidden");
                $("#usernamemessage2").addClass("hidden");
            } else if ($("#usernameinput").val().length == 0) {
                $("#usernamemessage2").removeClass("hidden");
                $("#usernamemessage").addClass("hidden");
            } else if ($("#usernameinput").val().length < 16 && $("#usernameinput").val().length != 0) {
                $("#usernamemessage").addClass("hidden");
                $("#usernamemessage2").addClass("hidden");
            } else {
                $("#usernamemessage").addClass("hidden");
                $("#usernamemessage2").addClass("hidden");
            }});
        $("#passwordinput").change(function(){
            if ($("#passwordinput2").val().length > 8) {
                ($("#passwordmessage2").addClass("text-success"));
            } else {
                ($("#passwordmessage2").removeClass("text-success"));
            }
            if ($("#passwordinput2").val().match(/[A-Z]/)) {
                ($("#passwordmessage3").addClass("text-success"));
                ($("#passwordmessage3").removeClass());
            }
        })
    </script>
</html>
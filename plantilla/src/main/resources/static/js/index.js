document.addEventListener('DOMContentLoaded', function() {
    const friendButton = document.getElementById('search');
    const userFind = document.getElementById('enterusername');

    friendButton.addEventListener('click', function() {
        if(userFind.style.display === "none"){
            userFind.style.display = "inline-block";
        }else{
            userFind.style.display = "none";
        }
    });
});
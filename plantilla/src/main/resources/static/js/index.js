document.addEventListener('DOMContentLoaded', function() {
    const friendButton = document.getElementById('search');
    const userFind = document.getElementById('username');

    friendButton.addEventListener('click', function() {
        userFind.classList.toggle("friendOcultar");
    });
});
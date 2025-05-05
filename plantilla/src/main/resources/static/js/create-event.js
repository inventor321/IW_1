document.addEventListener('DOMContentLoaded', function() {
    const eventDateInput = document.getElementById('eventDate');
    if (eventDateInput) {
        const now = new Date();
        const formattedDate = now.toISOString().slice(0, 16); // Formato: YYYY-MM-DDTHH:mm
        eventDateInput.value = formattedDate;
    }

    const urlSource = document.getElementById('urlSource');
    const fileSource = document.getElementById('fileSource');
    const urlInput = document.getElementById('urlInput');
    const fileInput = document.getElementById('fileInput');

    urlSource.addEventListener('change', function() {
        urlInput.style.display = 'block';
        fileInput.style.display = 'none';
    });

    fileSource.addEventListener('change', function() {
        urlInput.style.display = 'none';
        fileInput.style.display = 'block';
    });

    document.querySelector("#fetchImg").onclick = e => {
        let url = document.querySelector("#imageUrl").value;
        console.log("fetching ", url);
        readImageUrlData(url, document.querySelector("#ver"));
    };

    imageFile.onchange = e => {
        const [file] = imageFile.files;
        if (file) {
            ver2.src = URL.createObjectURL(file);
        }
    };
});

document.addEventListener('DOMContentLoaded', function() {
    const urlSource = document.getElementById('urlSource');
    const fileSource = document.getElementById('fileSource');
    const urlInput = document.getElementById('urlInput');
    const fileInput = document.getElementById('fileInput');

    urlSource.addEventListener('change', function() {
        urlInput.style.display = 'block';
        fileInput.style.display = 'none';
    });

    fileSource.addEventListener('change', function() {
        urlInput.style.display = 'none';
        fileInput.style.display = 'block';
    });

    document.querySelector("#fetchImg").onclick = e => {
        let url = document.querySelector("#imageUrl").value;
        console.log("fetching ", url);
        readImageUrlData(url, document.querySelector("#ver"));
    };

    imageFile.onchange = e => {
        const [file] = imageFile.files
        if (file) {
            ver2.src = URL.createObjectURL(file)
        }
    }
});
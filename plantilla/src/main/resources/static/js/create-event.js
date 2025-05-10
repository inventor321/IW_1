document.addEventListener('DOMContentLoaded', function () {
    // Establecer la fecha actual en el campo de fecha
    const eventDateInput = document.getElementById('eventDate');
    if (eventDateInput) {
        const now = new Date();
        const formattedDate = now.toISOString().slice(0, 16); // Formato: YYYY-MM-DDTHH:mm
        eventDateInput.value = formattedDate;
    }

    // Manejar la selección de fuente de imagen (URL o archivo local)
    const urlSource = document.getElementById('urlSource');
    const fileSource = document.getElementById('fileSource');
    const urlInput = document.getElementById('urlInput');
    const fileInput = document.getElementById('fileInput');

    if (urlSource && fileSource && urlInput && fileInput) {
        urlSource.addEventListener('change', function () {
            urlInput.style.display = 'block';
            fileInput.style.display = 'none';
        });

        fileSource.addEventListener('change', function () {
            urlInput.style.display = 'none';
            fileInput.style.display = 'block';
        });
    }

    // Manejar el botón "Search Image"
    const fetchImgButton = document.getElementById('fetchImg');
    const imageUrlInput = document.getElementById('imageUrl');
    const imagePreview = document.getElementById('ver');

    if (fetchImgButton && imageUrlInput && imagePreview) {
        fetchImgButton.addEventListener('click', function (e) {
            e.preventDefault(); // Evita cualquier acción predeterminada del botón

            const imageUrl = imageUrlInput.value;
            if (imageUrl) {
                imagePreview.src = imageUrl; // Establece la URL de la imagen en el elemento <img>
                imagePreview.alt = "Image Preview";
                imagePreview.style.display = 'block'; // Asegúrate de que la imagen sea visible
            } else {
                alert("Por favor, introduce una URL válida.");
            }
        });
    }

    // Manejar la vista previa de la imagen cargada localmente
    const fileInputElement = document.getElementById('imageFile');
    const filePreview = document.getElementById('ver2');

    if (fileInputElement && filePreview) {
        fileInputElement.addEventListener('change', function () {
            const [file] = fileInputElement.files;
            if (file) {
                filePreview.src = URL.createObjectURL(file); // Muestra la vista previa de la imagen cargada localmente
                filePreview.alt = "Image Preview";
                filePreview.style.display = 'block'; // Asegúrate de que la imagen sea visible
            }
        });
    }
});
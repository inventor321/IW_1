document.addEventListener('DOMContentLoaded', function () {
    // Profile photo handling code
    const profilePhotoForm = document.getElementById('profilePhotoForm');
    const urlSource = document.getElementById('urlSource');
    const fileSource = document.getElementById('fileSource');
    const urlInput = document.getElementById('urlInput');
    const fileInput = document.getElementById('fileInput');
    const imageUrlInput = document.getElementById('imageUrl');
    const imageFileInput = document.getElementById('imageFile');
    const previewUrl = document.getElementById('ver');
    const previewFile = document.getElementById('ver2');
    const fetchImgBtn = document.getElementById('fetchImg');

    // Toggle input visibility
    urlSource?.addEventListener('change', () => {
        urlInput.style.display = 'block';
        fileInput.style.display = 'none';
    });

    fileSource?.addEventListener('change', () => {
        urlInput.style.display = 'none';
        fileInput.style.display = 'block';
    });

    // Preview image handlers
    fetchImgBtn?.addEventListener('click', () => {
        const url = imageUrlInput.value;
        if (url) {
            previewUrl.src = url;
        }
    });

    imageFileInput?.addEventListener('change', function () {
        if (this.files && this.files[0]) {
            const file = this.files[0];
            previewFile.src = URL.createObjectURL(file);
        }
    });

    // User data update handling
    const saveButton = document.getElementById('save');
    if (saveButton) {
        saveButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            const userId = document.getElementById('username').getAttribute('data-user-id');
            const csrfToken = document.querySelector('meta[name="_csrf"]').content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
            
            const userData = {
                username: document.getElementById('username').value,
                firstName: document.getElementById('fname').value,
                email: document.getElementById('email').value,
                phonenumber: document.getElementById('pnumber').value
            };

            fetch(`/user/${userId}/update`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(userData)
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.text();
            })
            .then(result => {
                const alert = document.createElement('div');
                alert.className = 'alert alert-success position-fixed top-0 end-0 m-3';
                alert.innerHTML = '<i class="bi bi-check-circle me-2"></i>Datos actualizados correctamente';
                document.body.appendChild(alert);
                setTimeout(() => alert.remove(), 3000);
                setTimeout(() => location.reload(), 1500);
            })
            .catch(error => {
                console.error('Error:', error);
                const alert = document.createElement('div');
                alert.className = 'alert alert-danger position-fixed top-0 end-0 m-3';
                alert.innerHTML = `<i class="bi bi-exclamation-triangle me-2"></i>${error.message}`;
                document.body.appendChild(alert);
                setTimeout(() => alert.remove(), 3000);
            });
        });
    }

    // Profile photo form submission
    profilePhotoForm?.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData();
        const userId = document.getElementById('username').getAttribute('data-user-id');
        const imageSource = document.querySelector('input[name="imageSource"]:checked').value;

        if (imageSource === 'url') {
            const imageUrl = document.getElementById('imageUrl').value;
            if (!imageUrl) {
                alert('Por favor, introduce una URL válida');
                return;
            }

            try {
                // Fetch the image from the URL first
                const response = await fetch(imageUrl);
                const blob = await response.blob();

                // Create a File object from the blob
                const file = new File([blob], 'profile.jpg', { type: 'image/jpeg' });
                formData.append('photo', file);
                formData.append('imageSource', 'file');
            } catch (error) {
                console.error('Error fetching image:', error);
                alert('Error al obtener la imagen desde la URL');
                return;
            }
        } else {
            const fileInput = document.getElementById('imageFile');
            if (!fileInput.files.length) {
                alert('Por favor, selecciona un archivo');
                return;
            }
            formData.append('photo', fileInput.files[0]);
            formData.append('imageSource', 'file');
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;

        fetch(`/user/${userId}/pic`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            body: formData
        })
            .then(response => {
                if (!response.ok) throw new Error('Error al actualizar la imagen');
                return response.text();
            })
            .then(() => {
                alert('Foto de perfil actualizada correctamente');
                location.reload();
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message);
            });
    });
});
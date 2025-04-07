document.addEventListener('DOMContentLoaded', function() {
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
    const postAvatarBtn = document.getElementById('postAvatar');

    // Toggle input visibility
    urlSource?.addEventListener('change', () => {
        urlInput.style.display = 'block';
        fileInput.style.display = 'none';
    });

    fileSource?.addEventListener('change', () => {
        urlInput.style.display = 'none';
        fileInput.style.display = 'block';
    });

    // Preview URL image
    fetchImgBtn.addEventListener('click', () => {
        const url = imageUrlInput.value;
        if (url) {
            previewUrl.src = url;
        }
    });

    // Preview file image
    imageFileInput.addEventListener('change', function(e) {
        if (this.files && this.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                previewFile.src = e.target.result;
            };
            reader.readAsDataURL(this.files[0]);
        }
    });

    // Handle form submission
    profilePhotoForm?.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(this);
        const userId = document.getElementById('username').getAttribute('data-user-id');

        // Remove any existing file or url
        formData.delete('photo');
        
        // Add the correct photo data
        if (urlSource.checked) {
            formData.append('photo', imageUrlInput.value);
            formData.append('isUrl', 'true');
        } else {
            if (imageFileInput.files.length > 0) {
                formData.append('photo', imageFileInput.files[0]);
                formData.append('isUrl', 'false');
            } else {
                alert('Por favor, selecciona una imagen');
                return;
            }
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        if (!csrfToken) {
            console.error('CSRF token not found');
            return;
        }

        fetch(`/user/${userId}/pic`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            body: formData
        })
        .then(response => response.text())
        .then(text => {
            try {
                const data = JSON.parse(text);
                if (data.error) {
                    throw new Error(data.error);
                }
                alert('Foto de perfil actualizada correctamente');
                location.reload();
            } catch (e) {
                if (text.includes('success')) {
                    alert('Foto de perfil actualizada correctamente');
                    location.reload();
                } else {
                    throw new Error(text || 'Error al actualizar la imagen');
                }
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert(error.message);
        });
    });

    const saveButton = document.getElementById('save');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const userId = usernameInput.getAttribute('data-user-id');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || 
                      document.querySelector('input[name="_csrf"]')?.value;

    if (!csrfToken) {
        console.error('CSRF token not found');
        return;
    }

    saveButton.addEventListener('click', function() {
        const userData = {
            username: usernameInput.value,
            email: emailInput.value
        };

        fetch(`/user/${userId}/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(userData)
        })
        .then(response => {
            if (response.ok) {
                alert('Datos actualizados correctamente');
                location.reload();
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Error al actualizar los datos');
                });
            }
        })
        .catch(error => {
            alert(error.message);
            console.error('Error:', error);
        });
    });

    // Preview URL image
    document.querySelector("#fetchImg")?.addEventListener('click', () => {
        const url = imageUrlInput.value;
        if (url) {
            previewUrl.src = url;
        }
    });

    // Preview file image
    imageFileInput?.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const file = this.files[0];
            previewFile.src = URL.createObjectURL(file);
        }
    });
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
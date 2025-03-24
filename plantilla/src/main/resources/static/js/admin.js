const toggles = document.querySelectorAll('.toggle');
        toggles.forEach(toggle => {
            toggle.addEventListener('click', (e) => {
                e.preventDefault();
                const form = e.target.parentElement;
                go(form.action, 'POST').then((d) => {
                    if (d.enabled) {
                        e.target.classList.remove('btn-primary');
                        e.target.classList.add('btn-danger');
                        e.target.textContent = 'Deshabilitar';
                    } else {
                        e.target.classList.remove('btn-danger');
                        e.target.classList.add('btn-primary');
                        e.target.textContent = 'Habilitar';
                    } 
                    console.log(d);
                });
            });
        });
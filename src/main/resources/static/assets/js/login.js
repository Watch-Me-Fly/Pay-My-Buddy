document.addEventListener("DOMContentLoaded", () => {

    const alertBox = document.getElementById('alertBox');
    const form = document.querySelector('form');

    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value.trim();
        });

        fetch('/login', {
            method: 'POST',
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams(data)
        })
            .then(response => responseHandler(response))
            .then(data => {
                if (data && data.error) {
                    messageBox('ERROR', data.error);
                }
            })
            .catch(error => {
                console.error("Login error:", error);
                messageBox('ERROR', "Une erreur est survenue.");
            });
    });

    function responseHandler(response) {
        if (response.ok) {
            window.location.href = '/user/profile.html';
        } else {
            return response.json();
        }
    }
    function messageBox(type, message) {
        alertBox.removeAttribute('hidden');
        alertBox.textContent = message;
        alertBox.classList.remove('alert-success', 'alert-danger');
        alertBox.classList.add(type === 'SUCCESS' ? 'alert-success' : 'alert-danger');
    }
});
document.addEventListener('DOMContentLoaded', () => {

    "use strict";

    // Fields
    const usernameField = document.getElementById('inputUsername');
    const emailField = document.getElementById('inputEmail');
    const passwordField = document.getElementById('inputPassword');
    const form = document.getElementById('signupForm');
    const alertBox = document.getElementById('alertBox');
    let boxMessage = '';

    form.addEventListener('submit', (e) => {

        e.preventDefault();

        let userdata = {
            username: usernameField.value,
            email: emailField.value,
            password: passwordField.value
        }

        fetch('http://localhost:8080/users/signup', {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userdata),
            redirect: "manual"
        })
            .then(response => responseHandler(response))
            .then(data => {
                boxMessage = 'Compte utilisateur créé avec succès ! Redirection en cours...';
                messageBox('SUCCESS', boxMessage)
                // Wait 2 seconds, then redirect to login page
                setTimeout(() => {
                    window.location.href = "/login.html";
                }, 3000);
            })
            .catch(error => {
                console.error("Signup error:", error)
                messageBox('ERROR', "Une erreur est survenue.");
            });
    });

    function responseHandler(response) {
        if (!response.ok) {
            return response.json();
        }
        return response.text();
    }
    function messageBox(type, message) {
        alertBox.removeAttribute('hidden');
        alertBox.textContent = message;
        alertBox.classList.remove('alert-success', 'alert-danger');
        alertBox.classList.add(type === 'SUCCESS' ? 'alert-success' : 'alert-danger');
    }
});
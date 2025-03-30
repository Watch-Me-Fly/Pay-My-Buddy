document.addEventListener("DOMContentLoaded", () => {

    "use strict";

    // fields
    let usernameField = document.getElementById('inputUsername');
    let emailField = document.getElementById('inputEmail');
    let passwordField = document.getElementById('inputMDP');
    // buttons and links
    const modifyButton = document.getElementById('modifyBtn');
    const deleteAccount = document.getElementById('deleteAccountLink');
    // form
    const profileForm = document.getElementById('profileForm');
    // messages
    let messageType = 'SUCCESS' || 'FAIL' || null;
    let resultMessage = '';
    let alertBox = document.getElementById('alertBox');

    let isEditing = false;

    // -------------------------
    // fetch user data in fields
    // -------------------------
    fetch("/api/user/profile")
        .then(response => {
            if (!response.ok) {throw new Error('Problème à la restitution des données');}
           return response.json();
        })
        .then(data => {
            usernameField.value = data.username;
            emailField.value = data.email;
        })
        .catch(error =>
            console.error(`Error fetching user profile: ${error}`));

    // -------------------------
    // Toggle
    // -------------------------
    modifyButton.addEventListener('click', () => {
        isEditing ? saveChanges() : enableEditing();
    });

    // -------------------------
    // delete account
    // -------------------------
    deleteAccount.addEventListener('click', (e) => {
        e.preventDefault();

        if (!confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
            return;
        }
        fetch("/api/user/profile", {
            method: 'DELETE',
            headers: {"Content-Type": "application/json"},
        })
        .then(response => {
            if (!response.ok) {throw new Error('Erreur à la suppression du compte')}
            messageBox('SUCCESS', 'Compte supprimé');
            // show the message for 2sec before redirecting to main page
            setTimeout(() => {
            window.location.href = "/";},
                2000);
        })
        .catch(error => {
            console.error(`Error fetching user profile: ${error.message}`)
            messageBox('FAIL', 'Suppression du compte a échoué');
        });

    });

    // -------------------------
    // functions
    // -------------------------
    function saveChanges() {
        const updatedUserData = {
            email: emailField.value,
            password: passwordField.value.trim() || null
        }

        if (emailField.value == null && passwordField.value == null) {
            messageBox('FAIL', 'Données invalides');
            return;
        }

        fetch("/api/user/profile", {
            method: 'POST',
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(updatedUserData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Profile update error');
            }
            messageBox('SUCCESS', 'Profil mis à jour avec succès !');
            disableEditing();
        })
        .catch(error => {
            console.error(`Error updating profile: ${error}`);
            messageBox('FAIL', 'Erreur lors de la mise à jour du profil.');
        });
    }

    function enableEditing() {
        emailField.removeAttribute('disabled');
        passwordField.removeAttribute('disabled');
        modifyButton.textContent = 'Enregistrer';
        isEditing = true;
    }

    function disableEditing() {
        emailField.setAttribute('disabled', 'true');
        passwordField.setAttribute('disabled', 'true');
        passwordField.value = '';
        modifyButton.textContent = 'Modifier';
        isEditing = false;
    }

    function messageBox(type, message) {
        alertBox.removeAttribute('hidden');
        alertBox.textContent = message;
        alertBox.classList.remove('alert-success', 'alert-danger');
        alertBox.classList.add(type === 'SUCCESS' ? 'alert-success' : 'alert-danger');
    }
});

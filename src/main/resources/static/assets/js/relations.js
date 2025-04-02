document.addEventListener("DOMContentLoaded", function () {

    const emailField = document.getElementById('relationEmail');
    const searchBtn = document.getElementById('searchBtn');
    const addBtn = document.getElementById('addBtn');
    const alertBox = document.getElementById('alertBox');
    const table = document.getElementById('connectionsTable');
    const tableBody = document.getElementById('connectionsTableBody');
    let userId;

    // get user data
    fetch('/users/email')
        .then(response => {
            if (!response.ok) {
                throw new Error('failed to fetch user data from session');
            } else {
                return response.json();
            }
        })
        .then(userData => {
            userId = userData.id;
            console.log(`user id from session = ${userId}`);
        })
        .catch(error => {
            console.error('Error fetching user data:', error);
        });

    // even listeners
    searchBtn.addEventListener('click', e => {
        init();

        const email = emailField.value.trim();
        if (email === '') {
            // if no email is provided : get all connections
            getAllConnections();
        } else {
            // get a specific connection
            getASpecificConnection(email);
        }

    });

    addBtn.addEventListener('click', e => {
        init();
        const email = emailField.value.trim();
        if (email === '') {
            messageBox('WARNING', 'Veuillez saisir une adresse mail à ajouter');
            return;
        }
        addConnection(email);
    });

    // functions
    function init() {
        alertBox.setAttribute('hidden', '');
        table.setAttribute('hidden', '');
        tableBody.innerHTML = '';
    }

    function getAllConnections() {
        init();

        fetch(`/users/${userId}/connections`, {
            credentials: 'include' // Ensure cookies are sent with the request
        })
            .then(response => {
                const contentType = response.headers.get('content-type');
                if (!contentType || !contentType.includes('application/json')) {
                    throw new TypeError("Oops, we haven't got JSON!");
                }

                if (!response.ok) {
                    return response.text().then(text => {
                        console.log(`Failed to fetch connections: ${text}`);
                        throw new Error(`Failed to fetch connections: ${text}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.length === 0) {
                    messageBox('WARNING', 'Aucune connection trouvée');
                    table.setAttribute('hidden', '');
                } else {
                    tableBody.innerHTML = '';
                    console.log(`data.length= ${data.length}`);
                    for (let i = 0; i < data.length; i++) {
                        populateConnectionsTable(data[i]);
                    }
                }
            })
            .catch(error => {
                console.error('Error fetching connections:', error);
                messageBox('ERROR', `Erreur lors de la récupération des connections : ${error.message}`);
            });
    }

    function getASpecificConnection(email) {
        init();
        fetch(`/users/connection/${email}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('User not found');
            }
            return response.json();
        })
        .then(data => {
            if (data.length === 0) {
                messageBox('WARNING', 'Vous n\'avez pas de liaison avec cette personne');
                table.setAttribute('hidden', '');
            } else {
                populateConnectionsTable(data);
            }
        })
            .catch(error => {
                messageBox('ERROR', 'Erreur lors de la recherche de la relation');
            });
    }

    function populateConnectionsTable(connection) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${connection.username}</td>
            <td>${connection.email}</td>
        `;
        tableBody.appendChild(row);

        table.removeAttribute('hidden');
    }

    function addConnection(email) {
        // search for user by Email
        fetch(`/users/find/${email}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('User not found');
            }
            return response.json();
        })
        .then(user => {
            fetch(`/users/add/${userId}/connections/${user.id}`, {
                method: 'PUT'
            })
            .then(response => {
                if (response.ok) {
                    messageBox('SUCCESS', 'Utilisateur ajouté à la liste des connections');
                    populateConnectionsTable(user);
                } else {
                    messageBox('ERROR', `Erreur lors de l\'ajout`);
                }
            });
        })
            .catch(error => {
                messageBox('ERROR', 'Erreur lors de la recherche de l\'utilisateur');
            });
    }

    function messageBox(type, message) {
        alertBox.removeAttribute('hidden');
        alertBox.textContent = message;
        alertBox.classList.remove('alert-success', 'alert-danger', 'alert-warning');

        switch (type) {
            case 'ERROR' :
                alertBox.classList.add('alert-danger');
                break;
            case 'SUCCESS' :
                alertBox.classList.add('alert-success');
                break;
            case 'WARNING' :
                alertBox.classList.add('alert-warning');
                break;
            default:
                break;
        }
    }

});
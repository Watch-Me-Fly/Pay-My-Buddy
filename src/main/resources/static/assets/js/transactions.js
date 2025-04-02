document.addEventListener("DOMContentLoaded", function () {

    const transactionForm = document.getElementById("transactionForm");
    const descriptionBox = document.getElementById('descriptionBox');
    const amountBox = document.getElementById('amountBox');
    const transactionsTable = document.getElementById('transactionsTable');
    const selectRelation = document.getElementById('selectRelation');
    const alertBox = document.getElementById('alertBox');
    let userId;

    // Fetch user info
    fetch('/users/email')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user data from session');
            }
            return response.json();
        })
        .then(userData => {
            userId = userData.id;
            getConnections();
            getTransactions();
        })
        .catch(error => {
            console.error('Error fetching user data:', error);
        });

    // Creating a transaction from form
    transactionForm.addEventListener('submit', (e) => {
        e.preventDefault();
        alertBox.setAttribute('hidden', '');

        const recipientUsername = selectRelation.value;
        const description = descriptionBox.value;
        const amount = parseFloat(amountBox.value);

        if (!recipientUsername) {
            messageBox('WARNING', 'Veuillez choisir le destinataire');
            return;
        }
        if (!amount || amount <= 0) {
            messageBox('WARNING', 'Veuillez entrer un montant valide');
            return;
        }

        getUserIdByEmail(recipientUsername)
            .then(recipientId => {
                const transaction = {
                    sender: { id: userId },
                    receiver: { id: recipientId },
                    description: description,
                    amount: amount,
                };

                return fetch('/transactions', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(transaction)
                });
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`Failed to create transaction: ${text}`);
                    });
                }

                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                } else {
                    return {};
                }
            })
            .then(() => {
                messageBox('SUCCESS', 'Transaction créée avec succès');
                // refresh list
                getTransactions();
                transactionForm.reset();
            })
            .catch(error => {
                console.error('Error creating transaction:', error);
                messageBox('ERROR', `Erreur lors de la création de la transaction : ${error.message}`);
            });
    });
    function getConnections() {
        fetch(`/users/${userId}/connections`, {
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`Failed to fetch connections: ${text}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.length === 0) {
                    messageBox('WARNING', 'Aucune connection trouvée');
                } else {
                    selectRelation.innerHTML = '<option selected disabled>Sélectionner une relation</option>';
                    data.forEach(connection => {
                        const option = document.createElement('option');
                        option.value = connection.email;
                        option.textContent = connection.username;
                        selectRelation.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching connections:', error);
                messageBox('ERROR', `Erreur lors de la récupération des connections : ${error.message}`);
            });
    }
    function getUserIdByEmail(email) {
        return fetch(`/users/find/${email}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch user ID');
                }
                return response.json();
            })
            .then(user => user.id);
    }
    function getTransactions() {
        fetch(`/transactions/user/${userId}`)
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`Failed to fetch transactions: ${text}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                transactionsTable.innerHTML = '';
                if (data.length === 0) {
                    messageBox('WARNING', 'Aucune transaction trouvée');
                } else {
                    transactionsTable.innerHTML = '';
                    data.forEach(transaction => {
                        populateTransactionsTable(transaction);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching transactions:', error);
                messageBox('ERROR', `Erreur lors de la récupération des transactions : ${error.message}`);
            });
    }
    function populateTransactionsTable(transaction) {
        const row = document.createElement('tr');
        row.innerHTML = `
        <td>${transaction.connectionName || 'Unknown'}</td>
        <td>${transaction.description || 'No description'}</td>
        <td>${transaction.amount ? transaction.amount.toFixed(2) : '0.00'}</td>
        <td>
            <button class="btn btn-sm btn-danger delete-btn" data-id="${transaction.id}">🗑</button>
        </td>
    `;
        transactionsTable.appendChild(row);
        addEventToDeleteButton();
    }
    function addEventToDeleteButton() {
        document.querySelectorAll(".delete-btn").forEach(button => {
            button.addEventListener('click', function () {
                const transactionId = this.getAttribute('data-id');
                fetch(`/transactions/${transactionId}`, { method: 'DELETE' })
                    .then(() => getTransactions())
                    .catch(error => {
                        console.error(`Error deleting transaction: ${error.message}`);
                        messageBox('ERROR', `Erreur lors de la suppression de la transaction`);
                    });
            });
        });
    }
    function messageBox(type, message) {
        alertBox.removeAttribute('hidden');
        alertBox.textContent = message;
        alertBox.classList.remove('alert-success', 'alert-danger', 'alert-warning');

        switch (type) {
            case 'ERROR':
                alertBox.classList.add('alert-danger');
                break;
            case 'SUCCESS':
                alertBox.classList.add('alert-success');
                break;
            case 'WARNING':
                alertBox.classList.add('alert-warning');
                break;
            default:
                break;
        }
    }
});

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import UserCard from '../components/users/UserCard';
import { UserDTO } from '../types/UserDTO';
import { Form } from 'react-bootstrap';
import { getAllUsers } from '../api/userApi';

const Social: React.FC = () => {
    const { token, userId } = useAuth();
    const [users, setUsers] = useState<UserDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (!token) return;
        const fetchUsers = async () => {
            try {
                const data = await getAllUsers(token);
                // Exclude the current user from the fetched data
                setUsers(data.filter(user => user.id !== userId));
            } catch (err) {
                setError('Failed to load users');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, [token, userId]);

    // Filter and sort users alphabetically by displayName
    const filteredAndSortedUsers = users
        .filter(user =>
            user.displayName.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .sort((a, b) => a.displayName.localeCompare(b.displayName));

    if (loading) return <div className="text-center mt-5">Loading...</div>;
    if (error) return <div className="text-center mt-5 text-danger">{error}</div>;

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex align-items-center" style={{ flex: 1 }}>
                    <Form.Control
                        type="text"
                        placeholder="Search users..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="me-2"
                        style={{ 
                            width: '26rem', 
                            borderColor: '#000000',
                            borderWidth: '0.05rem'
                        } as React.CSSProperties}
                    />
                    <style>
                    {`
                        .form-control::placeholder {
                            color: #6c757d;
                            opacity: 1;
                        }
                    `}
                    </style>
                </div>
            </div>
            {filteredAndSortedUsers.length > 0 ? (
                <div className="row">
                    {filteredAndSortedUsers.map(user => (
                        <div key={user.id} className="col-md-4">
                            <UserCard user={user} />
                        </div>
                    ))}
                </div>
            ) : (
                <div className="text-center mt-5">
                    <h1>No users found</h1>
                </div>
            )}
        </div>
    );
};

export default Social;
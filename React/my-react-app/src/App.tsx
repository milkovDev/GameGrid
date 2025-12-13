import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { UserProvider } from './contexts/UserContext';
import { RealtimeProvider } from './contexts/RealTimeContext';
import { NotificationProvider } from './contexts/NotificationContext';
import { MessageProvider } from './contexts/MessageContext';
import Login from './pages/Login';
import Home from './pages/Home';
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import NotFound from './pages/NotFound';
import Games from './pages/Games';
import MyProfile from './pages/MyProfile';
import Articles from './pages/Articles';
import './App.css';
import ScrollToTop from './components/common/ScrollToTop';
import Social from './pages/Social';
import UserProfile from './pages/UserProfile';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuth();
    
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    return <>{children}</>;
};

const AppContent: React.FC = () => {
    const { isAuthenticated } = useAuth();
    
    return (
        <div className='App-background'>
            {isAuthenticated && <Header />}
            <div className={`container-fluid min-vh-100 d-flex flex-column ${isAuthenticated ? 'mt-5' : ''}`}>
                <ScrollToTop />
                <Routes>
                    <Route path="/" element={<ProtectedRoute><Home /></ProtectedRoute>} />
                    <Route path="/games" element={<ProtectedRoute><Games /></ProtectedRoute>} />
                    <Route path="/articles" element={<ProtectedRoute><Articles /></ProtectedRoute>} />
                    <Route path="/social" element={<ProtectedRoute><Social /></ProtectedRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
                    <Route path="/users/:userId" element={<ProtectedRoute><UserProfile /></ProtectedRoute>} />

                    <Route path="/login" element={<Login />} />
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </div>
            <Footer />
        </div>
    );
};

const App: React.FC = () => {
    return (
        <AuthProvider>
            <UserProvider>
                <RealtimeProvider>
                    <NotificationProvider>
                        <MessageProvider>
                            <Router>
                                <AppContent />
                            </Router>
                        </MessageProvider>
                    </NotificationProvider>
                </RealtimeProvider>
            </UserProvider>
        </AuthProvider>
    );
};

export default App;
import { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import Navbar from '../components/Navbar';
import { User, Calendar, Mail, Phone, MapPin, Edit2, Save, X, Shield } from 'lucide-react';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isEditing, setIsEditing] = useState(false);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    dateOfBirth: '',
    address: '',
    city: '',
    state: '',
    country: '',
    postalCode: '',
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await userAPI.getProfile();
      setProfile(response.data);
      setFormData({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        phoneNumber: response.data.phoneNumber || '',
        dateOfBirth: response.data.dateOfBirth || '',
        address: response.data.address || '',
        city: response.data.city || '',
        state: response.data.state || '',
        country: response.data.country || '',
        postalCode: response.data.postalCode || '',
      });
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      const response = await userAPI.updateProfile(formData);
      setProfile(response.data);
      setSuccess('Profile updated successfully!');
      setIsEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      firstName: profile.firstName || '',
      lastName: profile.lastName || '',
      phoneNumber: profile.phoneNumber || '',
      dateOfBirth: profile.dateOfBirth || '',
      address: profile.address || '',
      city: profile.city || '',
      state: profile.state || '',
      country: profile.country || '',
      postalCode: profile.postalCode || '',
    });
    setIsEditing(false);
    setError('');
    setSuccess('');
  };

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">My Profile</h1>
          <p className="text-gray-400 text-lg">Manage your personal information and settings</p>
        </div>

        {loading && (
          <div className="flex justify-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-yellow-400"></div>
          </div>
        )}

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl mb-6 flex items-center gap-2">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="bg-green-500/10 border border-green-500/30 text-green-400 p-4 rounded-xl mb-6 flex items-center gap-2">
            <span>✅</span>
            <span>{success}</span>
          </div>
        )}

        {!loading && profile && (
          <div className="space-y-6">
            {/* Account Information Card */}
            <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-xl">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 rounded-lg bg-yellow-500/10 flex items-center justify-center">
                  <Shield className="w-5 h-5 text-yellow-400" />
                </div>
                <h2 className="text-2xl font-bold text-white">Account Information</h2>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <InfoItem icon={<User className="w-5 h-5" />} label="User ID" value={profile.userId} mono />
                <InfoItem icon={<User className="w-5 h-5" />} label="Username" value={profile.username} />
                <InfoItem icon={<Mail className="w-5 h-5" />} label="Email" value={profile.email} />
                <InfoItem icon={<Calendar className="w-5 h-5" />} label="Member Since" value={new Date(profile.createdAt).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })} />
              </div>
            </div>

            {/* Personal Information Card */}
            <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-xl">
              <div className="flex justify-between items-center mb-6">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-yellow-500/10 flex items-center justify-center">
                    <User className="w-5 h-5 text-yellow-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">Personal Information</h2>
                </div>
                {!isEditing && (
                  <button
                    onClick={() => setIsEditing(true)}
                    className="flex items-center gap-2 bg-yellow-500/10 hover:bg-yellow-500/20 text-yellow-400 px-5 py-2.5 rounded-lg border border-yellow-500/30 transition-all font-semibold"
                  >
                    <Edit2 className="w-4 h-4" />
                    Edit Profile
                  </button>
                )}
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* First Name */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      First Name
                    </label>
                    <input
                      type="text"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="John"
                    />
                  </div>

                  {/* Last Name */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Last Name
                    </label>
                    <input
                      type="text"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="Doe"
                    />
                  </div>

                  {/* Phone Number */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      value={formData.phoneNumber}
                      onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="+1 234 567 8900"
                    />
                  </div>

                  {/* Date of Birth */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Date of Birth
                    </label>
                    <input
                      type="date"
                      value={formData.dateOfBirth}
                      onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                    />
                  </div>

                  {/* Address */}
                  <div className="md:col-span-2">
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Address
                    </label>
                    <input
                      type="text"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="123 Main Street"
                    />
                  </div>

                  {/* City */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      City
                    </label>
                    <input
                      type="text"
                      value={formData.city}
                      onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="New York"
                    />
                  </div>

                  {/* State */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      State / Province
                    </label>
                    <input
                      type="text"
                      value={formData.state}
                      onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="NY"
                    />
                  </div>

                  {/* Country */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Country
                    </label>
                    <input
                      type="text"
                      value={formData.country}
                      onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="United States"
                    />
                  </div>

                  {/* Postal Code */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-300 mb-2">
                      Postal Code
                    </label>
                    <input
                      type="text"
                      value={formData.postalCode}
                      onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                      disabled={!isEditing}
                      className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none disabled:bg-[#0A0E27] disabled:cursor-not-allowed transition"
                      placeholder="10001"
                    />
                  </div>
                </div>

                {isEditing && (
                  <div className="flex space-x-4 pt-4">
                    <button
                      type="submit"
                      disabled={saving}
                      className="flex-1 bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-3 rounded-lg font-bold hover:from-yellow-500 hover:to-yellow-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-yellow-500/30 flex items-center justify-center gap-2"
                    >
                      {saving ? (
                        <>
                          <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin"></div>
                          Saving...
                        </>
                      ) : (
                        <>
                          <Save className="w-5 h-5" />
                          Save Changes
                        </>
                      )}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancel}
                      className="flex-1 bg-gray-700/50 hover:bg-gray-700 text-gray-300 py-3 rounded-lg font-bold transition-all flex items-center justify-center gap-2 border border-white/10"
                    >
                      <X className="w-5 h-5" />
                      Cancel
                    </button>
                  </div>
                )}
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const InfoItem = ({ icon, label, value, mono = false }) => (
  <div className="bg-[#1A1F37] p-4 rounded-lg border border-white/10">
    <div className="flex items-center gap-2 text-gray-400 mb-2">
      {icon}
      <span className="text-sm font-medium">{label}</span>
    </div>
    <p className={`text-white font-semibold ${mono ? 'font-mono text-sm' : ''}`}>
      {value || <span className="text-gray-500">Not set</span>}
    </p>
  </div>
);

export default Profile;
import { axiosClient } from './axiosClient';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../types';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    return response.data;
  },

  register: async (payload: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/register', payload);
    return response.data;
  },

  getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
    const response = await axiosClient.get<ApiResponse<UserResponse>>('/auth/me');
    return response.data;
  },
};

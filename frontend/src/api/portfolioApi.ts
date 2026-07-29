import { axiosClient } from './axiosClient';
import { ApiResponse, CreatePortfolioRequest, PortfolioResponse, UpdatePortfolioRequest } from '../types';

export const portfolioApi = {
  getAll: async (): Promise<ApiResponse<PortfolioResponse[]>> => {
    const response = await axiosClient.get<ApiResponse<PortfolioResponse[]>>('/portfolios');
    return response.data;
  },

  getById: async (id: string): Promise<ApiResponse<PortfolioResponse>> => {
    const response = await axiosClient.get<ApiResponse<PortfolioResponse>>(`/portfolios/${id}`);
    return response.data;
  },

  create: async (payload: CreatePortfolioRequest): Promise<ApiResponse<PortfolioResponse>> => {
    const response = await axiosClient.post<ApiResponse<PortfolioResponse>>('/portfolios', payload);
    return response.data;
  },

  update: async (id: string, payload: UpdatePortfolioRequest): Promise<ApiResponse<PortfolioResponse>> => {
    const response = await axiosClient.put<ApiResponse<PortfolioResponse>>(`/portfolios/${id}`, payload);
    return response.data;
  },

  delete: async (id: string): Promise<ApiResponse<void>> => {
    const response = await axiosClient.delete<ApiResponse<void>>(`/portfolios/${id}`);
    return response.data;
  },
};

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  enabled: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface PortfolioResponse {
  id: string;
  userId: string;
  name: string;
  description?: string;
  currency: string;
  totalValue: number;
  totalUnrealizedPnL: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePortfolioRequest {
  name: string;
  description?: string;
  currency?: string;
}

export interface UpdatePortfolioRequest {
  name?: string;
  description?: string;
  currency?: string;
}

export type Exchange = 'NSE' | 'BSE' | 'NASDAQ' | 'NYSE';
export type PositionType = 'LONG' | 'SHORT';
export type PositionStatus = 'OPEN' | 'PARTIALLY_CLOSED' | 'CLOSED';
export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';

export interface PositionResponse {
  id: string;
  portfolioId: string;
  symbol: string;
  exchange: Exchange;
  positionType: PositionType;
  quantity: number;
  entryPrice: number;
  currentPrice: number;
  stopLossPrice?: number;
  takeProfitPrice?: number;
  unrealizedPnL: number;
  status: PositionStatus;
  riskLevel: RiskLevel;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePositionRequest {
  portfolioId: string;
  symbol: string;
  exchange: Exchange;
  positionType: PositionType;
  quantity: number;
  entryPrice: number;
  stopLossPrice?: number;
  takeProfitPrice?: number;
}

export interface UpdatePositionRequest {
  quantity?: number;
  currentPrice?: number;
  stopLossPrice?: number;
  takeProfitPrice?: number;
  status?: PositionStatus;
}

export type RecommendationAction = 'HOLD' | 'BOOK_PROFIT' | 'ADD' | 'EXIT' | 'TIGHTEN_STOPLOSS';
export type RecommendationType = 'HOLD' | 'BOOK_PROFIT' | 'ADD' | 'EXIT' | 'TIGHTEN_STOPLOSS';

export interface PositionHealthReport {
  positionId: string;
  symbol: string;
  healthScore: number; // 0 to 100
  riskLevel: RiskLevel;
  recommendation: RecommendationAction;
  reason: string;
}

export interface RecommendationResponse {
  positionId: string;
  symbol: string;
  recommendation: RecommendationType;
  confidence: number; // 0 to 100
  reason: string;
}

export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

export interface AlertResponse {
  id: string;
  userId: string;
  portfolioId: string;
  positionId: string;
  title: string;
  message: string;
  severity: AlertSeverity;
  read: boolean;
  createdAt: string;
}

export interface PortfolioSummaryDto {
  totalPortfolioValue: number;
  totalUnrealizedPnL: number;
  todayPnL: number;
}

export interface HealthSummaryDto {
  overallHealthScore: number;
  healthyPositions: number;
  warningPositions: number;
  criticalPositions: number;
}

export interface DashboardResponse {
  portfolioSummary: PortfolioSummaryDto;
  healthSummary: HealthSummaryDto;
  recentAlerts: AlertResponse[];
  recommendations: RecommendationResponse[];
  openPositions: PositionResponse[];
}

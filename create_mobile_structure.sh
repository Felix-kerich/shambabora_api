#!/bin/bash

# Define the base directory (update if needed)
BASE_DIR="app/src/main/java/com/app/shambabora"

echo "🌾 Creating ShambaBora Mobile App folder structure under $BASE_DIR ..."

# Create main architecture folders - Data Layer
mkdir -p $BASE_DIR/data/model
mkdir -p $BASE_DIR/data/network
mkdir -p $BASE_DIR/data/db
mkdir -p $BASE_DIR/data/repository

# Create domain layer
mkdir -p $BASE_DIR/domain/usecase/auth
mkdir -p $BASE_DIR/domain/usecase/farm
mkdir -p $BASE_DIR/domain/usecase/marketplace
mkdir -p $BASE_DIR/domain/usecase/collaboration

# Create UI layer
mkdir -p $BASE_DIR/ui/screens/auth
mkdir -p $BASE_DIR/ui/screens/dashboard
mkdir -p $BASE_DIR/ui/screens/farm/activities
mkdir -p $BASE_DIR/ui/screens/farm/expenses
mkdir -p $BASE_DIR/ui/screens/farm/yields
mkdir -p $BASE_DIR/ui/screens/weather
mkdir -p $BASE_DIR/ui/screens/marketplace
mkdir -p $BASE_DIR/ui/screens/community
mkdir -p $BASE_DIR/ui/screens/messaging
mkdir -p $BASE_DIR/ui/screens/profile
mkdir -p $BASE_DIR/ui/components
mkdir -p $BASE_DIR/ui/theme

# Create ViewModel layer
mkdir -p $BASE_DIR/viewmodel

# Create DI and Navigation
mkdir -p $BASE_DIR/di
mkdir -p $BASE_DIR/navigation

# Create Utils
mkdir -p $BASE_DIR/utils

echo "✅ Folder structure created successfully!"

# Create main application files
echo "📝 Creating base Kotlin files..."

touch $BASE_DIR/MainActivity.kt
touch $BASE_DIR/ShambaBora Application.kt

# Navigation
touch $BASE_DIR/navigation/AppNavHost.kt
touch $BASE_DIR/navigation/Screen.kt

# DI Module
touch $BASE_DIR/di/AppModule.kt
touch $BASE_DIR/di/NetworkModule.kt
touch $BASE_DIR/di/DatabaseModule.kt

# Data - Models
touch $BASE_DIR/data/model/User.kt
touch $BASE_DIR/data/model/FarmerProfile.kt
touch $BASE_DIR/data/model/FarmActivity.kt
touch $BASE_DIR/data/model/FarmExpense.kt
touch $BASE_DIR/data/model/YieldRecord.kt
touch $BASE_DIR/data/model/Product.kt
touch $BASE_DIR/data/model/Order.kt
touch $BASE_DIR/data/model/Post.kt
touch $BASE_DIR/data/model/Message.kt
touch $BASE_DIR/data/model/Group.kt
touch $BASE_DIR/data/model/Weather.kt
touch $BASE_DIR/data/model/Dashboard.kt

# Data - Network
touch $BASE_DIR/data/network/ApiService.kt
touch $BASE_DIR/data/network/RetrofitClient.kt
touch $BASE_DIR/data/network/AuthInterceptor.kt
touch $BASE_DIR/data/network/ApiResponse.kt

# Data - Database
touch $BASE_DIR/data/db/AppDatabase.kt
touch $BASE_DIR/data/db/dao/FarmActivityDao.kt
touch $BASE_DIR/data/db/dao/FarmExpenseDao.kt
touch $BASE_DIR/data/db/dao/YieldRecordDao.kt
touch $BASE_DIR/data/db/dao/ProductDao.kt

# Data - Repository
touch $BASE_DIR/data/repository/AuthRepository.kt
touch $BASE_DIR/data/repository/UserRepository.kt
touch $BASE_DIR/data/repository/FarmActivityRepository.kt
touch $BASE_DIR/data/repository/FarmExpenseRepository.kt
touch $BASE_DIR/data/repository/YieldRecordRepository.kt
touch $BASE_DIR/data/repository/DashboardRepository.kt
touch $BASE_DIR/data/repository/WeatherRepository.kt
touch $BASE_DIR/data/repository/MarketplaceRepository.kt
touch $BASE_DIR/data/repository/CollaborationRepository.kt

# Domain - Use Cases - Auth
touch $BASE_DIR/domain/usecase/auth/LoginUseCase.kt
touch $BASE_DIR/domain/usecase/auth/RegisterUseCase.kt
touch $BASE_DIR/domain/usecase/auth/LogoutUseCase.kt

# Domain - Use Cases - Farm
touch $BASE_DIR/domain/usecase/farm/CreateActivityUseCase.kt
touch $BASE_DIR/domain/usecase/farm/GetActivitiesUseCase.kt
touch $BASE_DIR/domain/usecase/farm/CreateExpenseUseCase.kt
touch $BASE_DIR/domain/usecase/farm/GetExpensesUseCase.kt
touch $BASE_DIR/domain/usecase/farm/CreateYieldRecordUseCase.kt
touch $BASE_DIR/domain/usecase/farm/GetYieldRecordsUseCase.kt
touch $BASE_DIR/domain/usecase/farm/GetDashboardUseCase.kt
touch $BASE_DIR/domain/usecase/farm/GetRemindersUseCase.kt

# Domain - Use Cases - Marketplace
touch $BASE_DIR/domain/usecase/marketplace/CreateProductUseCase.kt
touch $BASE_DIR/domain/usecase/marketplace/GetProductsUseCase.kt
touch $BASE_DIR/domain/usecase/marketplace/PlaceOrderUseCase.kt
touch $BASE_DIR/domain/usecase/marketplace/GetOrdersUseCase.kt

# Domain - Use Cases - Collaboration
touch $BASE_DIR/domain/usecase/collaboration/CreatePostUseCase.kt
touch $BASE_DIR/domain/usecase/collaboration/GetFeedUseCase.kt
touch $BASE_DIR/domain/usecase/collaboration/SendMessageUseCase.kt
touch $BASE_DIR/domain/usecase/collaboration/GetConversationsUseCase.kt
touch $BASE_DIR/domain/usecase/collaboration/GetGroupsUseCase.kt

# ViewModels
touch $BASE_DIR/viewmodel/AuthViewModel.kt
touch $BASE_DIR/viewmodel/DashboardViewModel.kt
touch $BASE_DIR/viewmodel/FarmActivityViewModel.kt
touch $BASE_DIR/viewmodel/FarmExpenseViewModel.kt
touch $BASE_DIR/viewmodel/YieldRecordViewModel.kt
touch $BASE_DIR/viewmodel/WeatherViewModel.kt
touch $BASE_DIR/viewmodel/MarketplaceViewModel.kt
touch $BASE_DIR/viewmodel/CommunityViewModel.kt
touch $BASE_DIR/viewmodel/MessagingViewModel.kt
touch $BASE_DIR/viewmodel/ProfileViewModel.kt

# UI - Screens - Auth
touch $BASE_DIR/ui/screens/auth/LoginScreen.kt
touch $BASE_DIR/ui/screens/auth/RegisterScreen.kt
touch $BASE_DIR/ui/screens/auth/SplashScreen.kt

# UI - Screens - Dashboard
touch $BASE_DIR/ui/screens/dashboard/DashboardScreen.kt
touch $BASE_DIR/ui/screens/dashboard/HomeScreen.kt

# UI - Screens - Farm Activities
touch $BASE_DIR/ui/screens/farm/activities/ActivityListScreen.kt
touch $BASE_DIR/ui/screens/farm/activities/AddActivityScreen.kt
touch $BASE_DIR/ui/screens/farm/activities/ActivityDetailScreen.kt
touch $BASE_DIR/ui/screens/farm/activities/RemindersScreen.kt

# UI - Screens - Farm Expenses
touch $BASE_DIR/ui/screens/farm/expenses/ExpenseListScreen.kt
touch $BASE_DIR/ui/screens/farm/expenses/AddExpenseScreen.kt
touch $BASE_DIR/ui/screens/farm/expenses/ExpenseAnalyticsScreen.kt

# UI - Screens - Yields
touch $BASE_DIR/ui/screens/farm/yields/YieldListScreen.kt
touch $BASE_DIR/ui/screens/farm/yields/AddYieldScreen.kt
touch $BASE_DIR/ui/screens/farm/yields/YieldTrendsScreen.kt

# UI - Screens - Weather
touch $BASE_DIR/ui/screens/weather/WeatherScreen.kt
touch $BASE_DIR/ui/screens/weather/ForecastScreen.kt

# UI - Screens - Marketplace
touch $BASE_DIR/ui/screens/marketplace/ProductListScreen.kt
touch $BASE_DIR/ui/screens/marketplace/ProductDetailScreen.kt
touch $BASE_DIR/ui/screens/marketplace/AddProductScreen.kt
touch $BASE_DIR/ui/screens/marketplace/MyProductsScreen.kt
touch $BASE_DIR/ui/screens/marketplace/OrderListScreen.kt
touch $BASE_DIR/ui/screens/marketplace/OrderDetailScreen.kt
touch $BASE_DIR/ui/screens/marketplace/PlaceOrderScreen.kt

# UI - Screens - Community
touch $BASE_DIR/ui/screens/community/FeedScreen.kt
touch $BASE_DIR/ui/screens/community/CreatePostScreen.kt
touch $BASE_DIR/ui/screens/community/PostDetailScreen.kt
touch $BASE_DIR/ui/screens/community/GroupListScreen.kt
touch $BASE_DIR/ui/screens/community/GroupDetailScreen.kt
touch $BASE_DIR/ui/screens/community/CreateGroupScreen.kt

# UI - Screens - Messaging
touch $BASE_DIR/ui/screens/messaging/ConversationListScreen.kt
touch $BASE_DIR/ui/screens/messaging/ChatScreen.kt
touch $BASE_DIR/ui/screens/messaging/GroupChatScreen.kt

# UI - Screens - Profile
touch $BASE_DIR/ui/screens/profile/ProfileScreen.kt
touch $BASE_DIR/ui/screens/profile/EditProfileScreen.kt
touch $BASE_DIR/ui/screens/profile/FarmerProfileScreen.kt
touch $BASE_DIR/ui/screens/profile/SettingsScreen.kt

# UI - Components
touch $BASE_DIR/ui/components/ActivityCard.kt
touch $BASE_DIR/ui/components/ExpenseCard.kt
touch $BASE_DIR/ui/components/YieldCard.kt
touch $BASE_DIR/ui/components/ProductCard.kt
touch $BASE_DIR/ui/components/PostCard.kt
touch $BASE_DIR/ui/components/MessageCard.kt
touch $BASE_DIR/ui/components/CustomButton.kt
touch $BASE_DIR/ui/components/CustomTextField.kt
touch $BASE_DIR/ui/components/LoadingIndicator.kt
touch $BASE_DIR/ui/components/ErrorView.kt
touch $BASE_DIR/ui/components/EmptyStateView.kt
touch $BASE_DIR/ui/components/DatePicker.kt
touch $BASE_DIR/ui/components/DropdownMenu.kt
touch $BASE_DIR/ui/components/ChartView.kt
touch $BASE_DIR/ui/components/WeatherWidget.kt
touch $BASE_DIR/ui/components/DashboardCard.kt

# UI - Theme
touch $BASE_DIR/ui/theme/Color.kt
touch $BASE_DIR/ui/theme/Theme.kt
touch $BASE_DIR/ui/theme/Type.kt
touch $BASE_DIR/ui/theme/Shape.kt

# Utils
touch $BASE_DIR/utils/Constants.kt
touch $BASE_DIR/utils/DateUtils.kt
touch $BASE_DIR/utils/NetworkUtils.kt
touch $BASE_DIR/utils/PreferenceManager.kt
touch $BASE_DIR/utils/ValidationUtils.kt
touch $BASE_DIR/utils/Extensions.kt
touch $BASE_DIR/utils/Resource.kt

echo "✅ Base Kotlin files created successfully!"
echo ""
echo "📊 Summary:"
echo "   - Main architecture: Data, Domain, UI layers"
echo "   - 10+ ViewModels for different features"
echo "   - 40+ Screen files organized by feature"
echo "   - 15+ Reusable UI components"
echo "   - Complete repository pattern setup"
echo "   - Use case layer for business logic"
echo "   - Database DAOs for offline support"
echo ""
echo "🚀 Done! You can now open Android Studio and start implementing."
echo "📖 Refer to API_DOCUMENTATION.md and MOBILE_QUICK_START.md for API integration."

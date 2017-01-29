angular
  .module('app', [
    'ui.router',
    'ui.bootstrap',
    'ui.bootstrap.tpls'
  ])
 .config(['$stateProvider', '$urlRouterProvider', '$httpProvider', function($stateProvider,
     $urlRouterProvider, $httpProvider) {
       $httpProvider.defaults.useXDomain = true;

       $urlRouterProvider.otherwise('login');
       $stateProvider
         .state('login', {
           url: '/login',
           views: {
             // Main template
             '': { templateUrl: '../views/login/login.html' },

             // Header
             'header@login': {
               templateUrl: '../views/header.html',
               controller: 'HeaderController'
             },

             // Login form
             'form@login': {
               templateUrl: '../views/login/form.html',
               controller: 'LoginController'
             },
           },
         })
         .state('logout', {
           url: '/logout',
           controller: 'LogoutController'
         })
         .state('forbidden-authentication', {
           url: '/forbidden-authentication',
           templateUrl: '../views/forbidden-authentification.html'
         })
         .state('reader', {
           url: '/reader',
           views: {
             // Main template
             '': { templateUrl: '../views/reader/reader.html'},

             // Header
             'header@reader': {
               templateUrl: '../views/header.html',
               controller: 'HeaderController'
             },

             // Login form
             'body@reader': {
               templateUrl: '../views/reader/body.html',
               controller: 'ReaderController'
             },
         },
         authenticate: true
        })
        //  .state('forbidden-permission', {
        //    url: '/forbidden-permission',
        //    templateUrl: 'views/forbidden-permission.html',
        //  });
}])
.run(['$rootScope', '$state', '$window', function($rootScope, $state, $window) {
  $rootScope.$on('$stateChangeStart', function(event, next, nextParams, previous, previousParams) {
    // redirect to login page if not logged in
    var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));
    if (next.authenticate && !currentUser) {
      console.log('Prevent');
      event.preventDefault(); //prevent current page from loading
      $state.go('forbidden-authentication');
      return;
    }

    // // Redirect to home page if the user doesn't have the required permissions
    // if (currentUser && !AuthService.hasPermission(next.permissions, currentUser.roles)) {
    //   event.preventDefault(); //prevent current page from loading
    //   $state.go('forbidden-permission');
    //   return;
    // }
  });
}]);

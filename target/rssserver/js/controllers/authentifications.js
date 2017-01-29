'use strict';

angular
  .module('app')
  .controller('LoginController', ['$scope', '$rootScope', '$window', '$state', 'AuthService',
      function($scope, $rootScope, $window, $state, AuthService) {

    $scope.radioModel = 'SignUp';
    $scope.user = {};


    $scope.login = function() {
      AuthService.login($scope.user.email, $scope.user.password)
        .then(function() {
          var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));
          console.log('Current user:', currentUser);
          $state.go('reader');
        }, function(err) {
          console.log('Error while loging in', err);
        })
    };

    $scope.signUp = function() {
      AuthService.signUp($scope.user.newEmail, $scope.user.newPassword)
        .then(function() {
          var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));
          console.log('Current user:', currentUser);
          $state.go('reader');
        }, function(err) {
          console.log('Error while signing up', err);
        })
    }

  }])
  .controller('LogoutController', ['$scope', '$state', '$window', 'AuthService',
      function($scope, $state, $window, AuthService) {

    function logout() {
      AuthService.logout()
        .then(function() {
          $state.go('login');
        });
    }
    logout();

}]);

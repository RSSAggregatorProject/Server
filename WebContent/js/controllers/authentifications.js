'use strict';

angular
  .module('app')
  .controller('LoginController', ['$scope', '$rootScope', '$window', '$state', 'AuthService',
      function($scope, $rootScope, $window, $state, AuthService) {

    $scope.radioModel = 'SignUp';
    $scope.user = {};

    $scope.errorAlert = {
      msg: "Something unexpected happened, please try again!",
      active: false
    };

    // Alert's functions
    $scope.addAlert = function(msg) {
      $scope.errorAlert.active = true;
      if (msg) {
        $scope.errorAlert.msg = msg;
      }
    };

    $scope.closeAlert = function() {
      $scope.errorAlert.active = false;
    };
    // End Alert's functions


    $scope.login = function() {
      AuthService.login($scope.user.email, $scope.user.password)
        .then(function() {
          var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));
          console.log('Current user:', currentUser);
          $state.go('reader');
        }, function(err) {
          $scope.addAlert('Wrong combination email/password. Please ensure that your account truly exists!');
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
          $scope.addAlert('Sorry, we haven\'t been able to sign you up! This email address has already been used...');
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

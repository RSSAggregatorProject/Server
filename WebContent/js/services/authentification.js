angular
  .module('app')
  .factory('AuthService', ['$window', '$q', '$http', function($window, $q, $http) {
    function login(email, password) {
      return $q(function(resolve, reject) {
        // delete $http.defaults.headers.common['X-Requested-With'];
        var req = {
         method: 'POST',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/auth/',
         headers: {
           'Content-Type': 'application/json'
         },
         data: JSON.stringify({
           email: email,
           password: password
         })
       };

        $http(req).then(function (res) {
          $window.sessionStorage.setItem("currentUser", angular.toJson({
            id_user: res.data.id_user,
            token: res.data.token,
            email: email
          }));
          resolve();
        }, function(err) {
          console.log('An error has occured while logging in:', err);
          reject('Combination email/password incorrect');
        });
      });
    }

    function signUp(email, password) {
      return $q(function(resolve, reject) {

        var req = {
         method: 'POST',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/users/',
         headers: {
           'Content-Type': 'application/json'
         },
         data: JSON.stringify({
           email: email,
           password: password
         })
       };

        $http(req).then(function (res) {
          login(email, password).then(function() {
            resolve();
          }, function() {
            console.log('An error has occured while loggin in:', err);
            reject();
          })

        }, function (err) {
            console.log('An error has occured while setting a new account:', err);
            reject('Error while setting the new account:', err);
        });
      });
    }

    function logout() {
      return $q(function(resolve, reject) {
        $window.sessionStorage.setItem("currentUser", null);
        resolve();
      });
    }

    return {
      login: login,
      signUp: signUp,
      logout: logout
    };
  }]);

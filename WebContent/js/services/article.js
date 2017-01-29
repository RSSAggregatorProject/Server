angular
  .module('app')
  .factory('ArticleService', ['$window', '$q', '$http', function($window, $q, $http) {

    function starArticle(article) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'PUT',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/items/'+article.id_item,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: {
           "starred": true
         }
       };

        $http(req).then(function (res) {
          resolve();
        }, function(err) {
          console.log('An error has occured while starring article:', err);
          reject('Server returned an error while starring article: '+err);
        });

      });
    }

    function unstarArticle(article) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'PUT',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/items/'+article.id_item,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: {
           "starred": false
         }
       };

        $http(req).then(function (res) {
          resolve();
        }, function(err) {
          console.log('An error has occured while starring article:', err);
          reject('Server returned an error while starring article: '+err);
        });

      });
    }

    function readArticle(article) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'PUT',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/items/'+article.id_item,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: {
           "read": true
         }
       };

        $http(req).then(function (res) {
          var article = res;
          resolve(article);
        }, function(err) {
          console.log('An error has occured while starring article:', err);
          reject('Server returned an error while starring article: '+err);
        });

      });
    }


    return {
      readArticle: readArticle,
      starArticle: starArticle,
      unstarArticle: unstarArticle
    };
  }]);

angular
  .module('app')
  .factory('FeedService', ['$window', '$q', '$http', function($window, $q, $http) {
    function getFeeds(starred) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var url = 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/feeds/';
        if (starred) {
          url = url.concat('starred/');
        }
        var req = {
         method: 'GET',
         url: url,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         }
       };

        $http(req).then(function (res) {
          var feeds = res.data.data;
          resolve(feeds);
        }, function(err) {
          console.log('An error has occured while getting feeds:', err);
          reject('Server returned an error while getting feeds: '+err);
        });
      });
    }

    function getFeed(feed) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'GET',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/feeds/'+feed.id_feed,
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         }
       };

        $http(req).then(function (res) {
          var articles = res.data.data[0];
          resolve(articles);
        }, function(err) {
          console.log('An error has occured while getting articles:', err);
          reject('Server returned an error while getting articles: '+err);
        });

      });
    }

    function addFeed(feed) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'POST',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/feeds/',
         headers: {
           'Authorization': currentUser.token,
           'Content-Type': 'application/json'
         },
         data: JSON.stringify({
           'uri': feed.uri,
           'id_cat': feed.category.id_cat
         })
       };

        $http(req).then(function (res) {
            resolve(res.data);
          }, function(err) {
          console.log('An error has occured while logging in:', err);
          reject('Server returned an error while creating a new category: '+err);
        });
      });
    }

    function deleteFeed(feed) {
      return $q(function(resolve, reject) {
        var currentUser = JSON.parse($window.sessionStorage.getItem("currentUser"));

        var req = {
         method: 'DELETE',
         url: 'http://dreamteamrssfeader.ddns.net:8080/rssserver/api/feeds/'+feed.id_feed,
         headers: {
           'Authorization': currentUser.token
         }
        }

        $http(req).then(function (res) {
            resolve(res.data);
          }, function(err) {
          console.log('An error has occured while logging in:', err);
          reject('Server returned an error while creating a new feed: '+err);
        });

      });
    }


    return {
      getFeeds: getFeeds,
      getFeed: getFeed,
      addFeed: addFeed,
      deleteFeed: deleteFeed
    };
  }]);

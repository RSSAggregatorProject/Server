angular
  .module('app')
  .factory('FeedService', ['$window', '$q', function($window, $q) {
    function getFeeds(category) {
      return $q(function(resolve, reject) {
        // Request here
        // Temporary variables
        var err = null;
        var feeds = [
          {
            name: 'Feed 1'
          },
          {
            name: 'Feed 2'
          },
          {
            name: 'Feed 3'
          },
          {
            name: 'Feed 4'
          }
        ];
        // End temporary variables

        if (err) {
          reject('Server returned an error: '+err);
        }
        resolve(feeds);
      });
    }

    function addFeed(feed) {
      return $q(function(resolve, reject) {
        // Add a feed

        console.log('Create Feed:', feed);
      });
    }

    return {
      getFeeds: getFeeds,
      addFeed: addFeed,
    };
  }]);

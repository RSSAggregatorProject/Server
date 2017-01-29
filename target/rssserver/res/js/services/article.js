angular
  .module('app')
  .factory('ArticleService', ['$window', '$q', function($window, $q) {
    function getArticles() {
      return $q(function(resolve, reject) {
        // Request here
        // Temporary variables
        var err = null;
        var articles = [
          {
            title: 'First article!',
            content: 'This is the first article of the list.',
            open: false
          },
          {
            title: 'Here is another one',
            content: 'Another article',
            open: false
          },
          {
            title: 'Never two without three!',
            content: 'Last article of the list',
            open: false
          }
        ];
        // End temporary variables

        if (err) {
          reject('Server returned an error:'+err);
        }
        resolve(articles);
      });
    }

    return {
      getArticles: getArticles,
    };
  }]);

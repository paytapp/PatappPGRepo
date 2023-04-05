var gulp = require('gulp');
var sass = require('gulp-sass')(require('sass'));

gulp.task('hello', function() {
    console.log("hello i am in");
});

gulp.task('sass', function(){
    return gulp.src('scss/styles.scss')
      .pipe(sass()) // Converts Sass to CSS with gulp-sass
      .pipe(gulp.dest('public'))
});

gulp.task('watch', function(){
    gulp.watch('scss/**/*.scss', gulp.series('sass'));
    // Other watchers
  })
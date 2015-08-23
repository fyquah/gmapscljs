(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'gmapscljs.core
   :output-to "out/gmapscljs.js"
   :output-dir "out"})

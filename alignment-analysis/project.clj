(defproject alignment-analysis "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party
                               cljsjs/react]]
                 [thheller/shadow-cljs "2.11.8"]
                 [day8.re-frame/http-fx "0.2.1"]
                 [reagent "0.10.0"]
                 [cljsjs/react-select "2.4.4-0"]
                 [re-frame "1.0.0"]
                 [metasoarous/oz "1.6.0-alpha36"]]

  :plugins [[lein-shadow "0.2.0"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.9.0"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]


  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :shadow-cljs {:nrepl {:port 8777}
                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :compiler-options {:output-feature-set :es6}
                               :modules {:app {:init-fn alignment-analysis.core/init
                                               :preloads [devtools.preload]}}
                               :dev        {:compiler-options {:source-map      true
                                                               :closure-defines {re_frame.trace.trace_enabled?        true
                                                                                 day8.re-frame.tracing.trace-enabled? true}}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280
                                          :preloads  [devtools.preload
                                                      day8.re-frame-10x.preload]}
                               :release    {:compiler-options {:source-map    false
                                                               :optimizations :advanced
                                                               :infer-externs :auto}}}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]]
            "prod"         ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "karma"        ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]
                   [day8.re-frame/re-frame-10x "0.7.0"]]
    :source-paths ["dev"]}
   :prod {}}
  :prep-tasks [])

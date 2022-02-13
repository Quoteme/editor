(ns editor.core
  (:gen-class)
  (:require [lanterna.screen :as s])
  (:require [clojure.string :as cs]))

(defn drawUI
  "draw important information like the current mode on screen"
  [scr mode keyHistory]
  (let [ [width height] (s/get-size scr) ]
      (s/put-string scr 0 (- height 1)
        (str "MODE: " mode " " "Keys: " keyHistory)
        { :bg :white :fg :black})
      )
    )

(defn drawFile
  "write the file currently opened to screen"
  [scr file topline]
  (doseq [[lineNum line] (map-indexed vector file)]
    (s/put-string scr 0 (+ topline lineNum) line))
  )

(defn -main
  ; start the editor with an empty file
  ([] (do
        (println "Opening blank file")
        (-main "")))
  ; start the editor with a file argument
  ([fileName & args]
   (let [
         file (atom (if (= fileName "") [""] (cs/split-lines (slurp fileName))))
         supportedModes #{:normal :insert}
         mode (ref :normal :validator #(contains? supportedModes %))
         topline (atom 0) ; the line of the document currently shown on top
         posX (ref 0 :validator #(>= % 0)) ; position of cursor
         posY (ref 0 :validator #(>= % 0)) ; position of cursor
         keyHistory (ref [])
         keepRunning (atom true)
         scr (s/get-screen :swing )
         width (first (s/get-size scr))
         height (second (s/get-size scr))
         ]
         ( println "starting editor!" )
         (println file)
         ;; Start a new Terminal for ncurses/clojure-lanterna
         (s/in-screen scr
           (println "editor started! Entering event loop")
           (drawUI scr @mode @keyHistory)
           (s/redraw scr)
           (while keepRunning
             (do
               (s/clear scr)
               ; get new key input
               (dosync
                 (commute keyHistory
                        (fn [keyHistory' input]
                          (if (= input :escape) [] (conj keyHistory' input)))
                        (s/get-key-blocking scr) )
                 (case @mode
                   :normal (case (last @keyHistory)
                             \i (ref-set mode :insert)
                             \h (when (pos? @posX) (alter posX dec))
                             \j (commute posY inc)
                             \k (when (pos? @posY) (alter posY dec))
                             \l (commute posX inc)
                             nil)
                   :insert (case (last @keyHistory)
                             :escape (ref-set mode :normal)
                             nil)))
               (drawFile scr @file @topline)
               (s/move-cursor scr @posX @posY)
               (drawUI scr @mode @keyHistory)
               (s/redraw scr)
             ))
         )   
     )
  ))

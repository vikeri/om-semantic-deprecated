(ns search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan put! timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as gevents]))

(defn container-view [_ _ {:keys [class-name]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [input-component results-component]}]
      (dom/div #js {:className (str "ui search " class-name)}
               input-component results-component))))

(defn input-view [_ _ {:keys [class-name placeholder]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [focus-ch value-ch highlight-ch select-ch value highlighted-index]}]
      (dom/input
        #js {:type "text"
             :autoComplete "off"
             :spellCheck "false"
             :className (str "prompt " class-name)
             :placeholder placeholder
             :value value
             :onFocus #(put! focus-ch true)
             :onBlur #(go (let [_ (<! (timeout 100))]
                            ;; If we don't wait, then the dropdown will disappear before
                            ;; its onClick renders and a selection won't be made.
                            ;; This is a hack, of course, but I don't know how to fix it
                        (put! focus-ch false)))
             :onKeyDown (fn [e]
                          (case (.-keyCode e)
                            40 (put! highlight-ch (inc highlighted-index)) ;; up
                            38 (put! highlight-ch (dec highlighted-index)) ;; down
                            13 (put! select-ch highlighted-index) ;; enter
                            9  (put! select-ch highlighted-index) ;; tab
                            nil))
             :onChange #(put! value-ch (.. % -target -value))}))))

(defn results-view [app _ {:keys [class-name
                                  loading-view loading-view-opts
                                  render-item render-item-opts]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [highlight-ch select-ch value loading? focused? suggestions highlighted-index]}]
      (let [display? (and focused? value (not= value ""))
            ;display (if display? "block" "none")
            attrs #js {:className (str "results " (if display? "transition visible" "transition hidden") class-name)
                       ;:style #js {:display display}
                       }]
        (cond
         (and loading-view loading?)
         (dom/div attrs
                 (om/build loading-view app {:opts loading-view-opts}))

         (not (empty? suggestions))
         (apply dom/div attrs
                (map-indexed
                 (fn [idx item]
                   (om/build render-item app {:init-state
                                              {:highlight-ch highlight-ch
                                               :select-ch select-ch}
                                              :state
                                              {:item item
                                               :index idx
                                               :highlighted-index highlighted-index}
                                              :opts render-item-opts}))
                 suggestions))

         :otherwise (dom/div nil))))))


(defn render-item [app owner {:keys [class-name text-fn]}]
  (reify

    om/IDidMount
    (did-mount [this]
      (let [{:keys [index highlight-ch select-ch]} (om/get-state owner)
            node (om/get-node owner)]
        (gevents/listen node (.-MOUSEOVER gevents/EventType) #(put! highlight-ch index))
        (gevents/listen node (.-CLICK gevents/EventType) #(put! select-ch index))))

    om/IRenderState
    (render-state [_ {:keys [item index highlighted-index]}]
      (let [highlighted? (= index highlighted-index)]
        (dom/a #js {:className (str "result " (if highlighted? (str "active " class-name) class-name))
                    :href      "#" :onClick (fn [_] false)}

               (text-fn item index))))))

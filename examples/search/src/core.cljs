(ns examples.search.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [arosequist.om-autocomplete :as ac]
            [search :as search-semantic]
            [cljs.core.async :refer [<! chan put! timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn loading []
  (reify
    om/IRender
    (render [_]
      (dom/span nil " Loading..."))))

(defn suggestions [value suggestions-ch _]
  (go
    (<! (timeout 500))
    (put! suggestions-ch (mapv #(str value " " %) (range 1 10)))))

(defn render-result [item index]
      (dom/div #js{:className "content"}
               (dom/div #js{:className "title"} item)
               (dom/div #js{:className "description"} (str "Result number " index))))

(defn autocomplete [app owner]
  (reify
    om/IInitState
     (init-state [_]
       {:result-ch (chan)})

    om/IWillMount
    (will-mount [_]
      (let [result-ch (om/get-state owner :result-ch)]
        (go (loop []
          (let [[idx result] (<! result-ch)]
            (js/alert (str "Result is: " result))
            (recur))))))

    om/IRenderState
    (render-state [_ {:keys [result-ch]}]
      (om/build ac/autocomplete app
                {:opts
                 {:container-view search-semantic/container-view
                  :container-view-opts {}
                  :input-view search-semantic/input-view
                  :input-view-opts {:placeholder "Enter anything"}
                  :results-view search-semantic/results-view
                  :results-view-opts {:loading-view loading
                                      :render-item search-semantic/render-item
                                      :render-item-opts {:text-fn render-result}}
                  :result-ch result-ch
                  :suggestions-fn suggestions}}))))

(om/root autocomplete (atom {}) {:target (js/document.getElementById "autocomplete")})

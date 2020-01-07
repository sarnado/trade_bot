package com.currtex.tradebot.exchangeapi.json;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class GroupsResponseDecoder {

    private GroupStructure groups;

    public void decode(String json){


        Gson g = new Gson();

        groups = g.fromJson(json, GroupStructure.class);




    }

    public int getCountAsks(){


        return groups.getData().get("asks").size();

    }
    public int getCountBids(){

        return groups.getData().get("bids").size();

    }


    static class GroupStructure{
        private String status;
        private Map<String, List<Group>> data;

        public String getStatus() {
            return status;
        }

        public Map<String, List<Group>> getData() {
            return data;
        }

           static class Group{
                String price;
                String volume;

                public String getPrice() {
                    return price;
                }

                public String getVolume() {
                    return volume;
                }
            }
    }
}

package com.oneuphero.pogotracker;

import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PokemonSpawn extends SugarRecord {

//    @SerializedName("id")
//    @Expose
//    private Integer id;
    @SerializedName("pokedex_number")
    @Expose
    private Integer pokedexNumber;
    @SerializedName("latitude")
    @Expose
    private Float latitude;
    @SerializedName("longitude")
    @Expose
    private Float longitude;
    @SerializedName("expires_at")
    @Expose
    private Integer expiresAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("distance")
    @Expose
    private Float distance;
    @SerializedName("bearing")
    @Expose
    private String bearing;
    @SerializedName("pokemon_name")
    @Expose
    private String pokemonName;
    @SerializedName("pokemon_icon")
    @Expose
    private String pokemonIconUrl;

    public PokemonSpawn() {
        // default constructor is necessary for SugarRecord
    }

    /**
     *
     * @return
     * The id
     */
//    public Integer getId() {
//        return id;
//    }

    /**
     *
     * @param id
     * The id
     */
//    public void setId(Integer id) {
//        this.id = id;
//    }

    /**
     *
     * @return
     * The pokedexNumber
     */
    public Integer getPokedexNumber() {
        return pokedexNumber;
    }

    /**
     *
     * @param pokedexNumber
     * The pokedex_number
     */
    public void setPokedexNumber(Integer pokedexNumber) {
        this.pokedexNumber = pokedexNumber;
    }

    /**
     *
     * @return
     * The latitude
     */
    public Float getLatitude() {
        return latitude;
    }

    /**
     *
     * @param latitude
     * The latitude
     */
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @return
     * The longitude
     */
    public Float getLongitude() {
        return longitude;
    }

    /**
     *
     * @param longitude
     * The longitude
     */
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     * The expiresAt
     */
    public Integer getExpiresAt() {
        return expiresAt;
    }

    /**
     *
     * @param expiresAt
     * The expires_at
     */
    public void setExpiresAt(Integer expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     *
     * @return
     * The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     *
     * @param createdAt
     * The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     * @return
     * The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     *
     * @param updatedAt
     * The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     *
     * @return
     * The distance
     */
    public Float getDistance() {
        return distance;
    }

    /**
     *
     * @param distance
     * The distance
     */
    public void setDistance(Float distance) {
        this.distance = distance;
    }

    /**
     *
     * @return
     * The bearing
     */
    public String getBearing() {
        return bearing;
    }

    /**
     *
     * @param bearing
     * The bearing
     */
    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    /**
     *
     * @
     * The pokemon's name
     */
    public String getPokemonName() {
        return pokemonName;
    }

    /**
     *
     * @return
     * The URL for the pokemon icon
     */
    public String getPokemonIconUrl() {
        return pokemonIconUrl;
    }

    public String getTimeLeft() {
        Date date = new Date((long)expiresAt*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        return sdf.format(date);
    }

    public float distanceTo(Location location) {
        Location thisLoc = new Location("");
        thisLoc.setLatitude(latitude);
        thisLoc.setLongitude(longitude);
        return thisLoc.distanceTo(location);
    }

    public float bearingTo(Location location) {
        Location thisLoc = new Location("");
        thisLoc.setLatitude(latitude);
        thisLoc.setLongitude(longitude);
        return thisLoc.bearingTo(location);
    }
}
package com.oneuphero.pogotracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PokemonSpawn {

    @SerializedName("id")
    @Expose
    private Integer id;
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

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

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
}
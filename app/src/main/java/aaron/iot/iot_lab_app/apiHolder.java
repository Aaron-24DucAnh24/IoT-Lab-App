package aaron.iot.iot_lab_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface apiHolder {
    @GET("/auto")
    Call<Boolean> setAuto();

    @GET("/pumpOn")
    Call<String> pumpOn();

    @GET("/pumpOff")
    Call<String> pumpOff();

    @GET("/fanOn")
    Call<String> fanOn();

    @GET("/fanOff")
    Call<String> fanOff();

    @GET("/lightOn")
    Call<String> lightOn();

    @GET("/lightOff")
    Call<String> lightOff();

    @GET("/bounds")
    Call<String> getBounds();

    @GET("/{bounds}")
    Call<String> setBound(@Path("bounds") String bounds);
}

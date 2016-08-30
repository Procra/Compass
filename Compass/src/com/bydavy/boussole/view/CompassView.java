package com.bydavy.boussole.view;

//~--- non-JDK imports --------------------------------------------------------

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.bydavy.boussole.R;

/**
 * Class description
 *
 *
 */
public class CompassView extends View {
    //~--- fields -------------------------------------------------------------

    private float northOrientation = 0;

    private Paint circlePaint;
    private Paint northPaint;
    private Paint southPaint;
    
    private Path aiguille;

    //Délais entre chaque image
    private final int DELAY = 20;
    //Durée de l'animation
    private final int DURATION = 1000;
    
    private float startNorthOrientation;
    private float endNorthOrientation;
    
    //Heure de début de l’animation (ms)
    private long startTime;
    
    //Pourcentage d'évolution de l'animation
    private float perCent;
    //Temps courant
    private long curTime;
    //Temps total depuis le début de l'animation
    private long totalTime;
    
    private Runnable animationTask = new Runnable() {
        public void run() {
            curTime   = SystemClock.uptimeMillis();
            totalTime = curTime - startTime;

            if (totalTime > DURATION) {
                northOrientation = endNorthOrientation % 360;
                removeCallbacks(animationTask);
            } else {
                perCent = ((float) totalTime) / DURATION;

                // Animation plus réaliste de l'aiguille
                perCent          = (float) Math.sin(perCent * 1.5);
                perCent          = Math.min(perCent, 1);
                northOrientation = (float) (startNorthOrientation + perCent * (endNorthOrientation - startNorthOrientation));
                postDelayed(this, DELAY);
            }

            // on demande à notre vue de se redessiner
            invalidate();
        }
    };

    //~--- constructors -------------------------------------------------------

    // Constructeur par défaut de la vue
    public CompassView(Context context) {
        super(context);
        initView();
    }

    // Constructeur utilisé pour instancier la vue depuis sa
    // déclaration dans un fichier XML
    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    // idem au précédant
    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    //~--- get methods --------------------------------------------------------

    // permet de récupérer l'orientation de la boussole
    public float getNorthOrientation() {
        return northOrientation;
    }

    //~--- set methods --------------------------------------------------------

    // permet de changer l'orientation de la boussole
    public void setNorthOrientation(float rotation) {

        // on met à jour l'orientation uniquement si elle a changé
        if (rotation != this.northOrientation) {
            //Arrêter l'ancienne animation
            removeCallbacks(animationTask);
            
        	//Position courante
            this.startNorthOrientation = this.northOrientation;
            //Position désirée
            this.endNorthOrientation   = rotation;

            //Détermination du sens de rotation de l'aiguille
            if ( ((startNorthOrientation + 180) % 360) > endNorthOrientation)
            {
            	//Rotation vers la gauche
            	if ( (startNorthOrientation - endNorthOrientation) > 180 )
            	{
            		endNorthOrientation+=360;
            	}
            } else {
            	//Rotation vers la droite
            	if ( (endNorthOrientation - startNorthOrientation) > 180 )
            	{
            		startNorthOrientation+=360;
            	}
            }
            
            //Nouvelle animation
            startTime = SystemClock.uptimeMillis();
            postDelayed(animationTask, DELAY);
        }
    }

    //~--- methods ------------------------------------------------------------

    // Initialisation de la vue
    private void initView() {
        Resources r = this.getResources();

        // Paint pour l'arrière plan de la boussole
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.compassCircle));
        circlePaint.setStyle(Style.STROKE);

        // Paint pour les 2 aiguilles, Nord et Sud
        northPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        northPaint.setColor(r.getColor(R.color.northPointer));
        southPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        southPaint.setColor(r.getColor(R.color.southPointer));

        // Path pour dessiner les aiguilles
        aiguille = new Path();
    }

    // Permet de définir la taille de notre vue
    // /!\ par défaut un cadre de 100x100 si non redéfini
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth  = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        // Notre vue sera un carré, on garde donc le minimum
        int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    // Déterminer la taille de notre vue
    private int measure(int measureSpec) {
        int result   = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {

            // Taille par défaut
            result = 200;
        } else {

            // On va prendre la taille de la vue parente
            result = specSize;
        }

        return result;
    }

    // Appelée pour redessiner la vue
    @Override
    protected void onDraw(Canvas canvas) {
    	int textWidth = 0;
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;

        // On détermine le diamètre du cercle (arrière plan de la boussole)
        int radius = Math.min(centerX, centerY);

        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // On sauvegarde la position initiale du canvas
        canvas.save();

        // On tourne le canvas pour que le nord pointe vers le haut
        canvas.rotate(-northOrientation, centerX, centerY);

        // on créer une forme triangulaire qui part du centre du cercle et
        // pointe vers le haut
        aiguille.reset();
        aiguille.moveTo(centerX - 5, 70);
        aiguille.lineTo(centerX, 30);
        aiguille.lineTo(centerX + 5, 70);

        // On désigne l'aiguille Nord
        canvas.drawPath(aiguille, circlePaint);
        
        // Graduation
        int lasti = 0;
        for(int i = 0; i < 360; i = i+15)
        {
        	canvas.rotate(-lasti, centerX, centerY);
        	canvas.rotate(i, centerX, centerY);
        	canvas.drawLine(centerX, 0, centerX, 15, circlePaint);
        	if(i%10 != 5)
        	{
        		textWidth = 10;
        		if(i<100){ textWidth = 6; }
        		if(i<10){ textWidth = 4; }
        		canvas.drawText(String.valueOf(i), centerX - textWidth, 25, circlePaint);
        	}
        	
        	lasti = i;
        }
        
        // On restaure
        canvas.restore();
        
        canvas.drawText("N", centerX - 4, centerY, circlePaint );
        
		textWidth = 16;
		if(northOrientation<100)
			textWidth = 13;
		if(northOrientation<10)
			textWidth = 10;
		
        canvas.drawText(String.valueOf((int)northOrientation) + "°", centerX - textWidth, centerY + 15, circlePaint );
    }
}
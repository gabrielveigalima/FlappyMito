package br.com.gabrielveigalima.flyingmito;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlyingMito extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture[] personagem;
	private Texture fundo;
	private Texture obstaculoBaixo;
	private Texture obstaculoTopo;
	private Texture gameOver;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle Circulo;
	private Rectangle retanguloObstaculoBaixo;
	private Rectangle retanguloObstaculoTopo;
	private ShapeRenderer shape;

	//Atributos de configuraçoes

	private float larguraDispositivo;
	private float alturaDispositivo;
	private int estadoJogo = 0;
	private int pontuacao = 0;
	private boolean verificaBatida = true;

	//Sons
	private Music helicoptero;
	private Music explosao;
	private Music falaBolsonaro;
	long startTime = TimeUtils.millis();
	private boolean verificaFala = true;

	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVetical;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float deltaTime ;
	private Random numeroRandomico;
	private float alturaEntreCanosRandomica;
	private boolean marcouPonto;


	//Câmera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WHIDTH = 768;
	private final float VIRTUAL_HEIGTH = 1020;

	@Override
	public void create () {
		batch = new SpriteBatch();
		numeroRandomico = new Random();
		Circulo = new Circle();
		/*
		retanguloObstaculoBaixo = new Rectangle();
		retanguloObstaculoTopo = new  Rectangle();
		shape = new ShapeRenderer();*/

		personagem = new Texture[3];
		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);
		mensagem = new BitmapFont();
		mensagem.setColor(Color.BLACK);
		mensagem.getData().setScale(3);
		personagem[0] = new Texture("bolsonaroFelizHelicoptero1.png");
		personagem[1] = new Texture("bolsonaroFelizHelicoptero2.png");
		personagem[2] = new Texture("bolsonaroFelizHelicoptero3.png");
		fundo = new Texture("fundo.png");
		gameOver = new Texture("game_over.png");

		obstaculoBaixo = new Texture("che.png");
		obstaculoTopo = new Texture("bandeiraGay.png");
		larguraDispositivo = VIRTUAL_WHIDTH;
		alturaDispositivo = VIRTUAL_HEIGTH;
		posicaoInicialVetical = alturaDispositivo / 2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;



		/**************************************
		 * Configuração da câmera
		 */
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WHIDTH/2,VIRTUAL_HEIGTH/2, 0);
		viewport = new StretchViewport(VIRTUAL_WHIDTH, VIRTUAL_HEIGTH, camera);

		espacoEntreCanos = 350;

		/************
		 * Configuração de audio
		 */
		helicoptero = Gdx.audio.newMusic(Gdx.files.internal("audio/helicoptero.mp3"));

		//music.play();
		//music.setVolume(0.5f);                 // sets the volume to half the maximum volume
		//music.setLooping(true);                // will repeat playback until music.stop() is called
		//music.stop();                          // stops the playback
		//music.pause();                         // pauses the playback

	}

	public FlyingMito() {
		super();
		startTime = TimeUtils.millis();
	}


	@Override
	public void render () {

		camera.update();

		//Limpar frames anteriores

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (TimeUtils.millis() > startTime + 3200) {
			helicoptero.setLooping(true);
		}

		deltaTime = Gdx.graphics.getDeltaTime();
		variacao += deltaTime * 10;
		if (variacao > 2) variacao = 0;

		if(estadoJogo == 0 ){//Jogo não iniciado
			if (Gdx.input.justTouched()){
				estadoJogo = 1;
				helicoptero.play();
			}
		}else {
			velocidadeQueda++;
			if (posicaoInicialVetical > 0 || velocidadeQueda < 0)
				posicaoInicialVetical = posicaoInicialVetical - velocidadeQueda;

			if (estadoJogo == 1) {
				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -15;
				}

				//Verifica se o cano saiu da tela
				if (posicaoMovimentoCanoHorizontal < - obstaculoTopo.getWidth()) {
					alturaEntreCanosRandomica = numeroRandomico.nextInt(400) - 100;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					marcouPonto = false;
				}

				//Verifica pontuação
				if (posicaoMovimentoCanoHorizontal < 120) {
					if (!marcouPonto) {
						marcouPonto = true;
						pontuacao++;
					}
				}
			}else {//Tela de game over
				if (Gdx.input.justTouched()){
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVetical = alturaDispositivo / 2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					helicoptero.play();
					verificaBatida = true;
					verificaFala = true;
					explosao.dispose();
					falaBolsonaro.dispose();
				}
			}
		}
		//Configuração dados de projeção da câmera
		batch.setProjectionMatrix( camera.combined);

		batch.begin();

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(obstaculoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica);
		batch.draw(obstaculoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo/2 - obstaculoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica);
		batch.draw(personagem[ (int) variacao], 120, posicaoInicialVetical);
		fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 50);
		if(estadoJogo == 2){
			batch.draw(gameOver,larguraDispositivo /2 - gameOver.getWidth() / 2, alturaDispositivo /2);
			mensagem.draw(batch,"Toque para Reiniciar!!!",larguraDispositivo / 2 - 200 ,alturaDispositivo / 2 - gameOver.getHeight() / 2 + 100);
		}

		batch.end();

		Circulo.set(
				120 + personagem[0].getWidth() / 2,
				posicaoInicialVetical + personagem[0].getHeight() / 2,
				personagem[0].getWidth() / 2);

		retanguloObstaculoBaixo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo/2 - obstaculoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica + 20,
				obstaculoBaixo.getWidth(),
				obstaculoBaixo.getHeight() - 90
		);
		retanguloObstaculoTopo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica + 50,
				obstaculoTopo.getWidth(),
				obstaculoTopo.getHeight()
		);

		/*Desenha formas
		shape.begin(ShapeRenderer.ShapeType.Filled);
		shape.circle(Circulo.x, Circulo.y, Circulo.radius);
		shape.rect(retanguloObstaculoBaixo.x,retanguloObstaculoBaixo.y,retanguloObstaculoBaixo.width, retanguloObstaculoBaixo.height );
		shape.rect(retanguloObstaculoTopo.x,retanguloObstaculoTopo.y,retanguloObstaculoTopo.width, retanguloObstaculoTopo.height );
		shape.setColor(Color.RED);
		shape.end();*/

		//Teste de colisão
		if (Intersector.overlaps(Circulo, retanguloObstaculoBaixo)
				|| Intersector.overlaps(Circulo, retanguloObstaculoTopo)
				|| posicaoInicialVetical <= 0 || posicaoInicialVetical >= alturaDispositivo){
			helicoptero.stop();

			if(verificaBatida){
				explosao = Gdx.audio.newMusic(Gdx.files.internal("audio/explosao.mp3"));
				explosao.play();
				verificaBatida = false;
			}
			if(Intersector.overlaps(Circulo, retanguloObstaculoBaixo)){
				if (verificaFala){
					falaBolsonaro = Gdx.audio.newMusic(Gdx.files.internal("audio/canalhas.mp3"));
					falaBolsonaro.play();
					verificaFala = false;
				}

			}if (Intersector.overlaps(Circulo, retanguloObstaculoTopo)){
				if (verificaFala){
					falaBolsonaro = Gdx.audio.newMusic(Gdx.files.internal("audio/rosquinha.mp3"));
					falaBolsonaro.play();
					verificaFala = false;
				}
			}
			marcouPonto = false;
			estadoJogo = 2; // Tela de game over
		}
	}
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

}

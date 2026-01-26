package hr.algebra.battleship.utils;

import hr.algebra.battleship.model.game.GameMove;
import hr.algebra.battleship.model.game.GameMoveTag;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility klasa za XML serijalizaciju game move-a s DOM parserom
 */
public class XmlUtils {

    private static final String DOCTYPE = "DOCTYPE";
    private static final String DTD = "dtd/gameMoves.dtd";
    private static final String GAME_MOVES = "GameMoves";
    private static final String FILENAME = "./xml/gameMoves.xml";

    private XmlUtils() {}

    /**
     * Sprema novi potez u XML datoteku
     */
    public static void saveNewMove(GameMove gameMove) {
        List<GameMove> gameMoveList;
        try {
            // ✅ Kreiraj xml i dat direktorije ako ne postoje
            Path xmlDir = Path.of("./xml");
            if (!Files.exists(xmlDir)) {
                Files.createDirectories(xmlDir);
                System.out.println("📁 Created xml directory");
            }

            Path datDir = Path.of("./dat");
            if (!Files.exists(datDir)) {
                Files.createDirectories(datDir);
                System.out.println("📁 Created dat directory");
            }

            gameMoveList = loadGameMoves();
            Document document = createDocument(GAME_MOVES);

            if (gameMoveList.isEmpty()) {
                appendGameMoveElement(gameMove, document);
            } else {
                gameMoveList.add(gameMove);
                for (GameMove nextGameMove : gameMoveList) {
                    appendGameMoveElement(nextGameMove, document);
                }
            }

            saveDocument(document, FILENAME);

        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Učitava sve poteze iz XML datoteke
     */
    public static List<GameMove> loadGameMoves() {
        return parse(FILENAME);
    }

    /**
     * Parsira XML datoteku i vraća listu GameMove-a
     */
    /**
     * Parsira XML datoteku i vraća listu GameMove-a
     */
    private static List<GameMove> parse(String path) {

        if (!Files.exists(Path.of(path))) {
            return new ArrayList<>();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // ✅ DISABLIRAJ DTD VALIDACIJU
        factory.setValidating(false);
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (Exception e) {
            System.err.println("⚠️ Warning setting XML features: " + e.getMessage());
        }

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.err.println("Warning: " + exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });

        Document document;
        try {
            document = builder.parse(new File(path));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return retrieveGameMoves(document);
    }


    /**
     * Izvlači sve GameMove-e iz DOM dokumenta
     */
    private static List<GameMove> retrieveGameMoves(Document document) {
        List<GameMove> gameMoves = new ArrayList<>();
        Element documentElement = document.getDocumentElement();
        NodeList nodes = documentElement.getElementsByTagName(GameMoveTag.GAME_MOVE.getTagName());

        for (int i = 0; i < nodes.getLength(); i++) {
            GameMove gameMove = new GameMove();
            Element item = (Element) nodes.item(i);

            // Očitaj vrijednosti iz XML-a
            gameMove.setRow(Integer.parseInt(
                    item.getElementsByTagName(GameMoveTag.ROW.getTagName()).item(0).getTextContent()));
            gameMove.setColumn(Integer.parseInt(
                    item.getElementsByTagName(GameMoveTag.COLUMN.getTagName()).item(0).getTextContent()));
            gameMove.setResult(
                    item.getElementsByTagName(GameMoveTag.RESULT.getTagName()).item(0).getTextContent());
            gameMove.setPlayerSymbol(
                    item.getElementsByTagName(GameMoveTag.PLAYER_SYMBOL.getTagName()).item(0).getTextContent());
            gameMove.setTimestamp(Long.parseLong(
                    item.getElementsByTagName(GameMoveTag.TIMESTAMP.getTagName()).item(0).getTextContent()));

            gameMoves.add(gameMove);
        }

        return gameMoves;
    }

    /**
     * Kreira novi XML dokument s root elementom
     */
    private static Document createDocument(String rootElement) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // ✅ Disabliraj validaciju i DTD
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
        } catch (Exception e) {
            System.err.println("⚠️ Warning: " + e.getMessage());
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // ✅ Kreiraj root element
        Element root = document.createElement(rootElement);
        document.appendChild(root);

        return document;
    }



    /**
     * Sprema DOM dokument u XML datoteku BEZ DTD-a
     */
    private static void saveDocument(Document document, String filename) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // ✅ NE ispisuj DOCTYPE
        transformer.transform(new DOMSource(document), new StreamResult(new File(filename)));
    }


    /**
     * Dodaje GameMove element u XML dokument
     */
    private static void appendGameMoveElement(GameMove gameMove, Document document) {
        Element element = document.createElement(GameMoveTag.GAME_MOVE.getTagName());
        document.getDocumentElement().appendChild(element);

        element.appendChild(createElement(document, GameMoveTag.ROW.getTagName(), String.valueOf(gameMove.getRow())));
        element.appendChild(createElement(document, GameMoveTag.COLUMN.getTagName(), String.valueOf(gameMove.getColumn())));
        element.appendChild(createElement(document, GameMoveTag.RESULT.getTagName(), gameMove.getResult()));
        element.appendChild(createElement(document, GameMoveTag.PLAYER_SYMBOL.getTagName(), gameMove.getPlayerSymbol()));
        element.appendChild(createElement(document, GameMoveTag.TIMESTAMP.getTagName(), String.valueOf(gameMove.getTimestamp())));
    }

    /**
     * Kreira XML element s tekstualnom vrijednosti
     */
    private static Node createElement(Document document, String tagName, String data) {
        Element element = document.createElement(tagName);
        Text text = document.createTextNode(data);
        element.appendChild(text);
        return element;
    }
}
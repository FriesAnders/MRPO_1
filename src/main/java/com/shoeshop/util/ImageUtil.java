package com.shoeshop.util;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Утилиты для работы с изображениями товаров.
 * Изображения сохраняются в папку product_images/ рядом с приложением.
 * Максимальный размер хранимого изображения: 300×200 пикселей.
 */
public final class ImageUtil {

    /** Целевые размеры изображений товаров (согласно заданию). */
    public static final int TARGET_WIDTH  = 300;
    public static final int TARGET_HEIGHT = 200;

    /** Папка для хранения фото товаров (рядом с рабочей директорией). */
    private static final Path IMAGES_DIR =
            Paths.get(System.getProperty("user.dir"), "product_images");

    private ImageUtil() {}

    /**
     * Копирует и масштабирует изображение из sourceFile в папку product_images.
     * Возвращает абсолютный путь к сохранённому файлу.
     *
     * @param sourceFile исходный файл изображения, выбранный пользователем
     * @param baseName   базовое имя для итогового файла (например, название товара)
     * @return путь к сохранённому файлу
     */
    public static String saveAndResize(File sourceFile, String baseName) throws IOException {
        Files.createDirectories(IMAGES_DIR);

        String ext      = getExtension(sourceFile.getName());
        String safeName = baseName.replaceAll("[^a-zA-Zа-яА-Я0-9_]", "_");
        String fileName = safeName + "_" + System.currentTimeMillis() + "." + ext;
        Path   destPath = IMAGES_DIR.resolve(fileName);

        BufferedImage original = ImageIO.read(sourceFile);
        if (original == null) {
            throw new IOException("Не удалось прочитать изображение. Убедитесь, что файл является корректным изображением.");
        }

        BufferedImage resized = resizeImage(original, TARGET_WIDTH, TARGET_HEIGHT);
        ImageIO.write(resized, ext.equals("jpg") ? "jpg" : ext, destPath.toFile());

        return destPath.toAbsolutePath().toString();
    }

    /**
     * Удаляет файл изображения по указанному пути.
     * Ошибки игнорируются (файл мог быть удалён вручную).
     */
    public static void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException ignored) {}
        }
    }

    /**
     * Загружает изображение для отображения в ListView.
     * Если путь не задан или файл не существует, возвращает картинку-заглушку picture.png.
     */
    public static Image loadProductImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            File file = new File(imagePath);
            if (file.exists()) {
                return new Image(file.toURI().toString(), 120, 100, true, true);
            }
        }
        return loadPlaceholder();
    }

    /**
     * Загружает картинку-заглушку из ресурсов (images/picture.png).
     */
    public static Image loadPlaceholder() {
        InputStream is = ImageUtil.class.getResourceAsStream("/images/picture.png");
        if (is != null) {
            return new Image(is, 120, 100, true, true);
        }
        // Если заглушка тоже не найдена — возвращаем пустой Image
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
    }

    // Масштабирование с сохранением качества
    private static BufferedImage resizeImage(BufferedImage src, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return result;
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0) {
            return fileName.substring(dot + 1).toLowerCase();
        }
        return "png";
    }
}
